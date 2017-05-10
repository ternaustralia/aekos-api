package au.org.aekos.api.producer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.apache.jena.query.Dataset;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.util.StreamUtils;

import au.org.aekos.api.producer.rdf.CoreDataAekosJenaModelFactory;
import au.org.aekos.api.producer.step.citation.AekosCitationRdfReader;
import au.org.aekos.api.producer.step.citation.in.InputCitationRecord;
import au.org.aekos.api.producer.step.species.AekosRelationalCsvWriter;
import au.org.aekos.api.producer.step.species.AekosSpeciesRdfReader;
import au.org.aekos.api.producer.step.species.SpeciesItemProcessor;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    
    @Value("${aekos-api.output-dir}")
    private String outputDir;

    @Bean
    public ItemReader<InputCitationRecord> readerCitation(String citationDetailsQuery, Dataset coreDS) {
        AekosCitationRdfReader result = new AekosCitationRdfReader();
        result.setCitationDetailsQuery(citationDetailsQuery);
        result.setDs(coreDS);
		return result;
    }
    
    @Bean
    public FlatFileItemWriter<InputCitationRecord> writerCitation() {
    	return csvWriter(InputCitationRecord.class, "citations", InputCitationRecord.getCsvFields());
    }

    @Bean
    public ItemReader<InputSpeciesRecord> readerSpecies(String darwinCoreAndTraitsQuery, Dataset coreDS) {
        AekosSpeciesRdfReader result = new AekosSpeciesRdfReader();
        result.setDwcAndTraitsQuery(darwinCoreAndTraitsQuery);
        result.setDs(coreDS);
		return result;
    }
    
    @Bean
    public SpeciesItemProcessor processorSpecies() {
        return new SpeciesItemProcessor();
    }

    @Bean
    public ItemWriter<OutputSpeciesWrapper> writer(FlatFileItemWriter<OutputSpeciesRecord> writerSpecies, FlatFileItemWriter<TraitRecord> writerTraitRecord) {
    	AekosRelationalCsvWriter result = new AekosRelationalCsvWriter();
    	result.setSpeciesWriter(writerSpecies);
    	result.setTraitWriter(writerTraitRecord);
		return result;
    }

    @Bean
    public FlatFileItemWriter<OutputSpeciesRecord> writerSpecies() throws Throwable {
    	return csvWriter(OutputSpeciesRecord.class, "species", OutputSpeciesRecord.getCsvFields());
    }

    @Bean
    public FlatFileItemWriter<TraitRecord> writerTraitRecord() throws Throwable {
    	return csvWriter(TraitRecord.class, "traits", TraitRecord.getCsvFields());
    }

	@Bean
    public Job apiDataJob(JobCompletionNotificationListener listener, Step citationStep, Step speciesStep) {
        return jobBuilderFactory.get("apiDataJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(citationStep)
                .next(speciesStep)
                // TODO add step for env data
                .build();
    }

    @Bean
    public Step citationStep(ItemReader<InputCitationRecord> reader, ItemWriter<InputCitationRecord> writer) {
        return stepBuilderFactory.get("step1_citation")
                .<InputCitationRecord, InputCitationRecord> chunk(10)
                .reader(reader)
                .writer(writer)
                .build();
    }
    
    @Bean
    public Step speciesStep(ItemReader<InputSpeciesRecord> reader, ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> processor,
    		ItemWriter<OutputSpeciesWrapper> writer) {
        return stepBuilderFactory.get("step2_species")
                .<InputSpeciesRecord, OutputSpeciesWrapper> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
    
    @Bean
    public Dataset coreDS(CoreDataAekosJenaModelFactory loader) {
    	return loader.getDatasetInstance();
    }
    
    @Bean
    public String citationDetailsQuery() throws IOException {
		return getSparqlQuery("citation-details.rq");
    }
    
    @Bean
    public String darwinCoreAndTraitsQuery() throws IOException {
		return getSparqlQuery("darwin-core-and-traits.rq");
    }
    
    @Bean
    public String environmentalVariablesQuery() throws IOException {
		return getSparqlQuery("environmental-variables.rq");
    }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
    
    private <T> FlatFileItemWriter<T> csvWriter(Class<T> type, String outputTableName, String[] fields) {
    	FlatFileItemWriter<T> result = new FlatFileItemWriter<>();
    	result.setShouldDeleteIfExists(true);
    	BeanWrapperFieldExtractor<T> fieldEx = new BeanWrapperFieldExtractor<>();
    	fieldEx.setNames(fields);
    	DelimitedLineAggregator<T> lineAgg = new DelimitedLineAggregator<>();
    	lineAgg.setFieldExtractor(fieldEx);
    	result.setLineAggregator(lineAgg);
    	result.setResource(new PathResource(Paths.get(outputDir, outputTableName + ".csv")));
		return result;
	}
}
