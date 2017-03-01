package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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

import au.org.aekos.api.loader.service.index.RAMDirectoryTermIndexManager;
import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.index.Trait;
import au.org.aekos.api.loader.service.load.LuceneIndexingService.IndexLoaderCallback;
import au.org.aekos.api.loader.service.load.LuceneIndexingService.SurveyDetails;

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
	public void testDoIndexing01() throws Throwable {
		String result = objectUnderTest.doIndexing();
		assertDocTypeCount("Should have processed the 2 VISITVIEWs", IndexConstants.DocTypes.ENV_RECORD, 2);
		assertDocTypeCount("Should have processed the 5 SPECIESORGANISMGROUPs", IndexConstants.DocTypes.SPECIES_RECORD, 5);
		assertThat(result, startsWith("Processed 5 records"));
		assertTotalRecordCountIs(7);
		// FIXME do we need to test the taxonRemarks field too?
		assertSpeciesHasTraits("Dampiera lavandulacea Lindl.",
			unitsTrait("averageHeight", "0.2", "metres"),
			noUnitsTrait("cover", "Isolated plants"),
			noUnitsTrait("lifeForm", "Forb"));
		assertSpeciesHasTraits("Acacia ingrata Benth.",
			unitsTrait("averageHeight", "0.4", "metres"),
			noUnitsTrait("cover", "Isolated plants"),
			noUnitsTrait("lifeForm", "Shrub"));
		assertSpeciesHasTraits("Eutaxia cuneata Meisn.",
			unitsTrait("averageHeight", "0.6", "metres"),
			noUnitsTrait("cover", "Isolated plants"),
			noUnitsTrait("lifeForm", "Shrub"),
			noUnitsTrait("phenology", "in flower"));
		assertSpeciesHasTraits("Acacia octonervia R.S.Cowan & Maslin",
			traitSet(
				unitsTrait("averageHeight", "0.3", "metres"),
				noUnitsTrait("cover", "<10% cover"),
				noUnitsTrait("lifeForm", "Shrub")),
			traitSet(
				unitsTrait("averageHeight", "0.3", "metres"),
				noUnitsTrait("cover", "<10% cover"),
				noUnitsTrait("lifeForm", "Shrub"),
				noUnitsTrait("phenology", "in flower"))
			);
		assertEnvRecords(
			envRecord("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054", "2007-04-20",
				vars("disturbanceType", "surfaceType", "soilTexture", "pH", "totalOrganicCarbon", "electricalConductivity")),
			envRecord("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054", "2009-05-21",
				vars("slope", "aspect"))
		);
	}

	/**
	 * Is the callback called the expected number of times?
	 */
	@Test
	public void testGetSpeciesIndexStream01() {
		objectUnderTest.collectCitationDetails();
		IndexLoaderCallback callback = mock(IndexLoaderCallback.class);
		objectUnderTest.getSpeciesIndexStream(callback);
		verify(callback, times(5)).accept(any());
	}
	
	/**
	 * Can we collect the citation details?
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCollectCitationDetails01() throws Throwable {
		Field f = LuceneIndexingService.class.getDeclaredField("citationRecords");
		f.setAccessible(true);
		objectUnderTest.collectCitationDetails();
		Map<String, SurveyDetails> citationRecords = (Map<String, SurveyDetails>) f.get(objectUnderTest);
		assertThat(citationRecords.size(), is(1));
		SurveyDetails theRecord = citationRecords.get("aekos.org.au/collection/wa.gov.au/ravensthorpe");
		assertThat(theRecord.getBibliographicCitation(), startsWith("Department of Parks and Wildlife"));
		assertThat(theRecord.getDatasetName(), is("Biological Survey of the Ravensthorpe Range (Phase 1)"));
	}

	private void assertDocTypeCount(String reason, String recordTypeCode, int expectedCount) throws IOException {
		Query query = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, recordTypeCode));
		int onlyAfterTotalHitsDontCareAboutScoreDocs = 1;
		TopDocs td = indexMgr.getIndexSearcher().search(query, onlyAfterTotalHitsDontCareAboutScoreDocs);
		assertThat(reason, td.totalHits, is(expectedCount));
	}
	
	/**
	 * Call directly to assert trait content for a species with greater than 1 record.
	 * 
	 * @param speciesName	name of species in record
	 * @param traitSet		one or more trait sets, one per record. The order matters
	 */
	private void assertSpeciesHasTraits(String speciesName, ExpectedTraitSet...traitSet) throws Throwable {
		int expectedRecordCount = traitSet.length;
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_SPECIES, analyzer).parse("\"" + speciesName + "\""), Occur.MUST)
				.build();
		int onlyAfterTotalHits = 1;
		TopDocs td = indexMgr.getIndexSearcher().search(q, onlyAfterTotalHits);
		assertThat(String.format("%s should have %d records", speciesName, expectedRecordCount), td.totalHits, is(expectedRecordCount));
		int zeroBasedRecordIndex = 0;
		// This bit is a bit brittle as it depends on order
		for (ExpectedTraitSet curr : traitSet) {
			assertSpeciesHasTraits(speciesName, zeroBasedRecordIndex++, curr.traits.toArray(new ExpectedTrait[0]));
		}
	}
	
	private void assertSpeciesHasTraits(String speciesName, ExpectedTrait...traits) throws Throwable {
		assertSpeciesHasTraits(speciesName, 0, traits);
	}
	
	private void assertSpeciesHasTraits(String speciesName, int docIndex, ExpectedTrait...traits) throws Throwable {
		int expectedTraitCount = traits.length;
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_SPECIES, analyzer).parse("\"" + speciesName + "\""), Occur.MUST)
				.build();
		int onlyAfterSingleSpeciesRecord = docIndex + 1;
		TopDocs td = indexMgr.getIndexSearcher().search(q, onlyAfterSingleSpeciesRecord);
		Document doc = indexMgr.getIndexSearcher().doc(td.scoreDocs[docIndex].doc);
		assertThat(String.format("%s should have %d traits", speciesName, expectedTraitCount),
				doc.getFields(IndexConstants.FLD_TRAIT).length, is(expectedTraitCount));
		Collection<Trait> actualTraits = LuceneLoaderClient.getTraitsFrom(doc);
		for (ExpectedTrait curr : traits) {
			assertThat(String.format("Expected '%s' to have a '%s'='%s' trait", speciesName, curr.name, curr.value),
					actualTraits, hasItem(new Trait(curr.name, curr.value, curr.units)));
		}
	}

	private void assertTotalRecordCountIs(int expectedCount) throws Throwable {
		Query q = new MatchAllDocsQuery();
		TopDocs td = indexMgr.getIndexSearcher().search(q, 1);
		assertThat(td.totalHits, is(expectedCount));
	}

	private static class ExpectedTrait {
		private final String name;
		private final String value;
		private final String units;

		public ExpectedTrait(String name, String value, String units) {
			this.name = name;
			this.value = value;
			this.units = units;
		}
	}
	
	private static ExpectedTrait noUnitsTrait(String name, String value) {
		return new ExpectedTrait(name, value, "");
	}
	
	private static ExpectedTrait unitsTrait(String name, String value, String units) {
		return new ExpectedTrait(name, value, units);
	}
	
	private static ExpectedTraitSet traitSet(ExpectedTrait...traits) {
		return new ExpectedTraitSet(Arrays.asList(traits));
	}
	
	private static class ExpectedTraitSet {
		private final Collection<ExpectedTrait> traits;

		public ExpectedTraitSet(Collection<ExpectedTrait> traits) {
			this.traits = traits;
		}
	}
	
	private class ExpectedEnvRecord {
		private final String locationId;
		private final String eventDate;
		private final Set<String> vars;

		public ExpectedEnvRecord(String locationId, String eventDate, Set<String> vars) {
			this.locationId = locationId;
			this.eventDate = eventDate;
			this.vars = vars;
		}
	}
	
	private ExpectedEnvRecord envRecord(String locationId, String eventDate, Set<String> vars) {
		return new ExpectedEnvRecord(locationId, eventDate, vars);
	}

	private Set<String> vars(String...vars) {
		return new HashSet<>(Arrays.asList(vars));
	}

	private void assertEnvRecords(ExpectedEnvRecord...expectedEnvRecords) throws Throwable {
		Query q = new TermQuery(new Term(IndexConstants.FLD_DOC_INDEX_TYPE, IndexConstants.DocTypes.ENV_RECORD.toLowerCase()));
		IndexSearcher indexSearcher = indexMgr.getIndexSearcher();
		int expectedRecordCount = expectedEnvRecords.length;
		TopDocs td = indexSearcher.search(q, expectedRecordCount);
		assertThat(String.format("Should have %d records", expectedRecordCount), td.totalHits, is(expectedRecordCount));
		int currentlyProcessingExpectedIndex = 0;
		for (ScoreDoc curr : td.scoreDocs) {
			Document doc = indexSearcher.doc(curr.doc);
			assertThat("LocationID doesn't match for expectation at index " + currentlyProcessingExpectedIndex,
					doc.get(IndexConstants.FLD_LOCATION_ID), is(expectedEnvRecords[currentlyProcessingExpectedIndex].locationId));
			assertThat("eventDate doesn't match for expectation at index " + currentlyProcessingExpectedIndex,
					doc.get(IndexConstants.FLD_EVENT_DATE), is(expectedEnvRecords[currentlyProcessingExpectedIndex].eventDate));
			IndexableField[] vars = doc.getFields(IndexConstants.FLD_ENVIRONMENT);
			assertThat("Variable collection length doesn't match for expectation at index " + currentlyProcessingExpectedIndex,
					vars.length, is(expectedEnvRecords[currentlyProcessingExpectedIndex].vars.size()));
			assertTrue("Variables content doesn't match for expectation at index " + currentlyProcessingExpectedIndex,
					Arrays.stream(vars)
					.map(e -> e.stringValue())
					.collect(Collectors.toList())
					.containsAll(expectedEnvRecords[currentlyProcessingExpectedIndex].vars));
			currentlyProcessingExpectedIndex++;
		}
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
	public TermIndexManager /*override the bean from the component scan*/FSDirectoryTermIndexManager() {
		return new RAMDirectoryTermIndexManager();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
	    PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
	    Properties properties = new Properties();
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