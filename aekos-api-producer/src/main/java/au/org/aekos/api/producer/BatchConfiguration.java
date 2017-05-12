package au.org.aekos.api.producer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import au.org.aekos.api.producer.rdf.CoreDataAekosJenaModelFactory;
import au.org.aekos.api.producer.step.citation.AekosCitationRdfReader;
import au.org.aekos.api.producer.step.citation.in.InputCitationRecord;
import au.org.aekos.api.producer.step.env.AekosEnvRdfReader;
import au.org.aekos.api.producer.step.env.AekosEnvRelationalCsvWriter;
import au.org.aekos.api.producer.step.env.EnvItemProcessor;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.EnvVarRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;
import au.org.aekos.api.producer.step.species.AekosSpeciesRdfReader;
import au.org.aekos.api.producer.step.species.AekosSpeciesRelationalCsvWriter;
import au.org.aekos.api.producer.step.species.SingleBagElementNoUnitsTraitExtractor;
import au.org.aekos.api.producer.step.species.SpeciesItemProcessor;
import au.org.aekos.api.producer.step.species.TraitExtractor;
import au.org.aekos.api.producer.step.species.UnitsBasedTraitExtractor;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
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

    @Value("${aekos-api.is-async}")
    private boolean isAsync;

    @Value("${aekos-api.property-namespace}")
	private String propertyNamespace;

    @Value("${aekos-api.common-graph-name}")
	private String commonGraphName;

    // [start citation config]
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
    // [end citation config]

    // [start species config]
    @Bean
    public ItemReader<InputSpeciesRecord> readerSpecies(String darwinCoreAndTraitsQuery, Dataset coreDS) {
        AekosSpeciesRdfReader result = new AekosSpeciesRdfReader();
        result.setDwcAndTraitsQuery(darwinCoreAndTraitsQuery);
        result.setDs(coreDS);
		return result;
    }
    
    @Bean
    public TraitExtractor heightExtractor(ExtractionHelper extractionHelper) {
    	UnitsBasedTraitExtractor result = new UnitsBasedTraitExtractor();
    	result.setHelper(extractionHelper);
    	result.setReferencingPropertyName("height");
		return result;
    }
    
    @Bean
    public TraitExtractor biomassExtractor(ExtractionHelper extractionHelper) {
    	UnitsBasedTraitExtractor result = new UnitsBasedTraitExtractor();
    	result.setHelper(extractionHelper);
    	result.setReferencingPropertyName("biomass");
		return result;
    }
    
    @Bean
    public TraitExtractor lifeStageExtractor(ExtractionHelper extractionHelper) { // FIXME need to crawl whole bag
    	SingleBagElementNoUnitsTraitExtractor result = new SingleBagElementNoUnitsTraitExtractor();
    	result.setHelper(extractionHelper);
    	result.setReferencingPropertyName("lifestage");
    	result.setNestedPropertyName("commentary");
		return result;
    }
    
    @Bean(destroyMethod="reportProblems")
    public SpeciesItemProcessor processorSpecies(List<TraitExtractor> traitExtractors, Dataset ds) {
        SpeciesItemProcessor result = new SpeciesItemProcessor();
        result.setExtractors(traitExtractors);
        result.setDataset(ds);
		return result;
    }

    @Bean
    public ItemWriter<OutputSpeciesWrapper> writerSpeciesWrapper(FlatFileItemWriter<OutputSpeciesRecord> writerSpecies, FlatFileItemWriter<TraitRecord> writerTraitRecord) {
    	AekosSpeciesRelationalCsvWriter result = new AekosSpeciesRelationalCsvWriter();
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
    // [end species config]

    // [start env config]
    @Bean
    public ItemReader<InputEnvRecord> readerEnv(String environmentalVariablesQuery, Dataset coreDS) {
        AekosEnvRdfReader result = new AekosEnvRdfReader();
        result.setEnvironmentalVariableQuery(environmentalVariablesQuery);
        result.setDs(coreDS);
		return result;
    }
    
    @Bean
    public EnvItemProcessor processorEnv() {
        return new EnvItemProcessor();
    }

    @Bean
    public ItemWriter<OutputEnvWrapper> writerEnvWrapper(FlatFileItemWriter<OutputEnvRecord> writerEnv, FlatFileItemWriter<EnvVarRecord> writerEnvVarRecord) {
    	AekosEnvRelationalCsvWriter result = new AekosEnvRelationalCsvWriter();
    	result.setEnvWriter(writerEnv);
    	result.setVariableWriter(writerEnvVarRecord);
		return result;
    }

    @Bean
    public FlatFileItemWriter<OutputEnvRecord> writerEnv() throws Throwable {
    	return csvWriter(OutputEnvRecord.class, "env", OutputEnvRecord.getCsvFields());
    }

    @Bean
    public FlatFileItemWriter<EnvVarRecord> writerEnvVarRecord() throws Throwable {
    	return csvWriter(EnvVarRecord.class, "envvars", EnvVarRecord.getCsvFields());
    }
    // [env env config]
    
    @Bean
    public ExtractionHelper extractionHelper(Dataset ds) {
    	ExtractionHelper result = new ExtractionHelper(propertyNamespace);
    	result.setCommonGraph(ds.getNamedModel(commonGraphName));
		return result;
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
    	if (isAsync) {
    		return new SimpleAsyncTaskExecutor("apiData"); // Consider changing to ThreadPoolTaskExecutor
    	}
    	return new SyncTaskExecutor();
    }
    
	@Bean
    public Job apiDataJob(JobCompletionNotificationListener listener, Step citationStep, Step speciesStep, Step envStep) {
        return jobBuilderFactory.get("apiDataJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(citationStep)
                .next(speciesStep)
                .next(envStep)
                .build();
    }

    @Bean
    public Step citationStep(ItemReader<InputCitationRecord> reader, ItemWriter<InputCitationRecord> writer , TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("step1_citation")
                .<InputCitationRecord, InputCitationRecord> chunk(10)
                .reader(reader)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }
    
    @Bean
    public Step speciesStep(ItemReader<InputSpeciesRecord> readerSpecies, ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> processorSpecies,
    		ItemWriter<OutputSpeciesWrapper> writerSpeciesWrapper, ItemReadListener<Object> readListener, ItemProcessListener<Object, Object> processListener,
    		TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("step2_species")
                .<InputSpeciesRecord, OutputSpeciesWrapper> chunk(1)
                .listener(readListener)
                .listener(processListener)
                .reader(readerSpecies)
                .processor(processorSpecies)
                .writer(writerSpeciesWrapper)
                .taskExecutor(taskExecutor)
                .build();
    }
    
    @Bean
    public Step envStep(ItemReader<InputEnvRecord> readerEnv, ItemProcessor<InputEnvRecord, OutputEnvWrapper> processorEnv,
    		ItemWriter<OutputEnvWrapper> writerEnvWrapper, ItemReadListener<Object> readListener, ItemProcessListener<Object, Object> processListener,
    		TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("step3_env")
                .<InputEnvRecord, OutputEnvWrapper> chunk(10)
                .listener(readListener)
                .listener(processListener)
                .reader(readerEnv)
                .processor(processorEnv)
                .writer(writerEnvWrapper)
                .taskExecutor(taskExecutor)
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
		return Utils.getResourceAsString("au/org/aekos/api/producer/sparql/" + fileName);
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
