package au.org.aekos.service.index;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.search.SearchService;
import au.org.aekos.service.search.StubVocabService;
import au.org.aekos.service.vocab.VocabService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=LuceneIndexingServiceTestContext.class)
public class LuceneIndexingServiceTest {

	@Autowired
	private LuceneIndexingService objectUnderTest;
	
	@Autowired
	private SearchService searchService;
	
	/**
	 * Can we create the index with the expected values?
	 */
	@Test
	public void testDoIndexing01() throws IOException {
		String result = objectUnderTest.doIndexing();
		assertThat(result, startsWith("Processed 8 records"));
		assertSpeciesHasRecords("Calotis hispidula", 2);
		assertSpeciesHasRecords("Goodenia havilandii", 1);
		assertSpeciesHasRecords("Rosa canina", 1);
		assertSpeciesHasRecords("Dodonaea concinna Benth.", 1);
		assertSpeciesHasRecords("Lasiopetalum compactum", 1);
		assertSpeciesHasRecords("Hakea obtusa", 1);
		assertSpeciesHasRecords("Acacia binata Maslin", 1);
		List<TraitOrEnvironmentalVariableVocabEntry> traits = searchService.getTraitVocabData();
		assertThat(traits.size(), is(6));
		assertThat(traits.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems(
				"totalLength", "lifeStage", "phenology", "heightOfBreak", "averageHeight", "cover"));
		List<TraitOrEnvironmentalVariableVocabEntry> envVars = searchService.getEnvironmentalVariableVocabData();
		assertThat(envVars.size(), is(18));
		assertThat(envVars.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems(
				"aspect", "clay", "disturbanceEvidenceCover", "disturbanceEvidenceType", "disturbanceType",
				"electricalConductivity", "erosionEvidenceState", "erosionEvidenceType", "latestLandUse", "ph",
				"sand", "silt", "slope", "soilTexture", "soilType",
				"surfaceType", "totalOrganicCarbon", "visibleFireEvidence"));
	}

	private void assertSpeciesHasRecords(String speciesName, int count) throws IOException {
		List<SpeciesSummary> searchResult = searchService.speciesAutocomplete(speciesName, 10);
		assertThat(searchResult.size(), is(1));
		SpeciesSummary speciesSummary = searchResult.get(0);
		assertThat(speciesSummary.getRecordsHeld(), is(count));
	}
}

@Configuration
@ComponentScan(
	basePackages={
		"au.org.aekos.service.retrieval",
		"au.org.aekos.service.index",
		"au.org.aekos.service.search"},
	excludeFilters={
		@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=au.org.aekos.service.retrieval.JossDataProvisionService.class)
	})
class LuceneIndexingServiceTestContext {
	
	@Bean
    public Dataset coreDS() {
    	Dataset result = DatasetFactory.create();
    	Model m = result.getDefaultModel();
    	m.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/test-darwin-core-plus-traits.ttl"), null, "TURTLE");
    	m.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/test-environmental-data.ttl"), null, "TURTLE");
		return result;
    }
	
	@Bean
	public VocabService vocabService() {
		return new StubVocabService();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
	    PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
	    Properties properties = new Properties();
	    properties.setProperty("lucene.index.createMode", "true");
	    properties.setProperty("lucene.index.wpath", "");
	    properties.setProperty("lucene.index.path", Files.createTempDirectory("testDoIndexing01").toString());
	    properties.setProperty("lucene.index.writer.commitLimit", "1000");
	    properties.setProperty("lucene.page.defaultResutsPerPage", "10");
	    properties.setProperty("aekos-api.owl-file.namespace", "http://www.aekos.org.au/ontology/1.0.0#");
		result.setProperties(properties);
		return result;
	}
	
    @Bean public String darwinCoreQueryTemplate() throws IOException { return getSparqlQuery("darwin-core.rq"); }
    @Bean public String darwinCoreCountQueryTemplate() throws IOException { return getSparqlQuery("darwin-core-count.rq"); }
    @Bean public String darwinCoreCountAllQueryTemplate() throws IOException { return getSparqlQuery("darwin-core-count-all.rq"); }
    @Bean public String environmentDataQueryTemplate() throws IOException { return getSparqlQuery("environment-data.rq"); }
    @Bean public String environmentDataCountQueryTemplate() throws IOException { return getSparqlQuery("environment-data-count.rq"); }
    @Bean public String traitDataCountQueryTemplate() throws IOException { return getSparqlQuery("trait-data-count.rq"); }
    @Bean public String indexLoaderQuery() throws IOException { return getSparqlQuery("index-loader.rq"); }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}