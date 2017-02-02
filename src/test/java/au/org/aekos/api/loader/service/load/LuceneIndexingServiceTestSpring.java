package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.load.IndexConstants;
import au.org.aekos.api.loader.service.load.LuceneIndexingService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=LuceneIndexingServiceTestContext.class)
public class LuceneIndexingServiceTestSpring {

	@Autowired
	private LuceneIndexingService objectUnderTest;
	
	@Autowired
	private TermIndexManager indexMgr;
	
	/**
	 * Can we create the index with the expected values?
	 */
	@Test
	public void testDoIndexing01() throws IOException {
		String result = objectUnderTest.doIndexing();
		assertDocTypeCount(IndexConstants.ENV_RECORD, 1);
		assertDocTypeCount(IndexConstants.SPECIES_RECORD, 5); // FIXME how many should we have?
		assertThat(result, startsWith("Processed 8 records"));
		assertSpeciesHasRecords("Calotis hispidula", 2);
		assertSpeciesHasRecords("Goodenia havilandii", 1);
		assertSpeciesHasRecords("Rosa canina", 1);
		assertSpeciesHasRecords("Dodonaea concinna Benth.", 1);
		assertSpeciesHasRecords("Lasiopetalum compactum", 1);
		assertSpeciesHasRecords("Hakea obtusa", 1);
		assertSpeciesHasRecords("Acacia binata Maslin", 1);
//		List<TraitOrEnvironmentalVariableVocabEntry> traits = searchService.getTraitVocabData();
//		assertThat(traits.size(), is(6));
//		assertThat(traits.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems(
//				"totalLength", "lifeStage", "phenology", "heightOfBreak", "averageHeight", "cover"));
//		List<TraitOrEnvironmentalVariableVocabEntry> envVars = searchService.getEnvironmentalVariableVocabData();
//		assertThat(envVars.size(), is(18));
//		assertThat(envVars.stream().map(e -> e.getCode()).collect(Collectors.toList()), hasItems(
//				"aspect", "clay", "disturbanceEvidenceCover", "disturbanceEvidenceType", "disturbanceType",
//				"electricalConductivity", "erosionEvidenceState", "erosionEvidenceType", "latestLandUse", "ph",
//				"sand", "silt", "slope", "soilTexture", "soilType",
//				"surfaceType", "totalOrganicCarbon", "visibleFireEvidence"));FIXME
	}

	private void assertDocTypeCount(String speciesRecord, int expectedCount) throws IOException {
		Query query = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.SPECIES_RECORD));
		TopDocs td = indexMgr.getIndexSearcher().search(query, 100);
		assertThat(td.totalHits, is(expectedCount));
	}

	private void assertSpeciesHasRecords(String speciesName, int count) throws IOException {
//		List<SpeciesSummary> searchResult = searchService.speciesAutocomplete(speciesName, 10);
//		assertThat(searchResult.size(), is(1));
//		SpeciesSummary speciesSummary = searchResult.get(0);
//		assertThat(speciesSummary.getRecordsHeld(), is(count));
	}
}

@Configuration
@ComponentScan(
	basePackages={
		"au.org.aekos.api.loader.service.index",
		"au.org.aekos.api.loader.service.load"})
class LuceneIndexingServiceTestContext {
	
	@Bean
    public Dataset coreDS() {
    	Dataset result = DatasetFactory.create();
    	Model m = result.getNamedModel("urn:some-model");
    	m.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/loader/test-citation-details.ttl"), null, "TURTLE");
    	m.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/loader/test-studylocationsubgraph-data.ttl"), null, "TURTLE");
		return result;
    }
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
	    PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
	    Properties properties = new Properties();
	    properties.setProperty("lucene.index.createMode", "true");
	    properties.setProperty("lucene.index.path", Files.createTempDirectory("testDoIndexing01").toString());
	    properties.setProperty("lucene.index.writer.commitLimit", "1000");
	    properties.setProperty("aekos-api.owl-file.namespace", "http://www.aekos.org.au/ontology/1.0.0#");
		result.setProperties(properties);
		return result;
	}
	
	@Bean public String darwinCoreAndTraitsQuery() throws IOException { return getSparqlQuery("darwin-core-and-traits.rq"); }
	@Bean public String citationDetailsQuery() throws IOException { return getSparqlQuery("citation-details.rq"); }
	@Bean public String environmentalVariablesQuery() throws IOException { return getSparqlQuery("environmental-variables.rq"); }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/loader/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}