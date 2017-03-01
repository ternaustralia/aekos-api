package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

import au.org.aekos.api.loader.service.SearchHelper;
import au.org.aekos.api.loader.service.index.RAMDirectoryTermIndexManager;
import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.index.Trait;

public class LuceneLoaderClientTest {
	
	/**
	 * Can we deserialse a trait array?
	 */
	@Test
	public void testGetTraitsFrom01() throws IOException {
		Document doc = new Document();
		String jsonTraitArray = "[{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"},{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}]";
		doc.add(new StoredField(IndexConstants.FLD_STORED_TRAITS, jsonTraitArray));
		Collection<Trait> result = LuceneLoaderClient.getTraitsFrom(doc);
		assertThat(result.size(), is(2));
		assertThat(result, hasItems(
				new Trait("height", "3", "metres"),
				new Trait("lifeForm", "Shrub", "")));
	}
	
	/**
	 * Can we serialse a trait array?
	 */
	@Test
	public void testWriteTraitsTo01() throws IOException {
		Document doc = new Document();
		Collection<Trait> traits = Arrays.asList(
				new Trait("height", "3", "metres"),
				new Trait("lifeForm", "Shrub", ""));
		LuceneLoaderClient.writeTraitsTo(doc, traits);
		String result = doc.get(IndexConstants.FLD_STORED_TRAITS);
		assertThat(result, is("[{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"},{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}]"));
	}
	
	/**
	 * Can we add a species record to the index?
	 */
	@Test
	public void testAddSpeciesRecord01() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				Set<Trait> traits = new HashSet<>();
				traits.add(new Trait("basalArea", "100", "utils"));
				traits.add(new Trait("averageHeight", "1.3", "metres"));
				traits.add(new Trait("lifeForm", "Forb", ""));
				SpeciesLoaderRecord record = new SpeciesLoaderRecord("species1", traits, "sampling protocol #1", "use it for whatever",
						"aekos.org.au/collection/loc1", "2012-12-30", "loc1", "Some Dataset");
				objectUnderTest.addSpeciesRecord(record);
			}
		},new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				int onlyAfterSingleDoc = 1;
				TopDocs td = searcher.search(new MatchAllDocsQuery(), onlyAfterSingleDoc);
				assertThat("Should be exactly one record", td.totalHits, is(1));
				Document result = searcher.doc(td.scoreDocs[0].doc);
				assertThat(result.get(IndexConstants.FLD_SPECIES), is("species1"));
				assertThat(result.get(IndexConstants.FLD_SAMPLING_PROTOCOL), is("sampling protocol #1"));
				assertThat(result.get(IndexConstants.FLD_BIBLIOGRAPHIC_CITATION), is("use it for whatever"));
				String[] resultTraits = result.getValues(IndexConstants.FLD_TRAIT);
				assertThat(resultTraits.length, is(3));
				assertThat(Arrays.asList(resultTraits), hasItems("basalArea","averageHeight","lifeForm"));
				assertThat(LuceneLoaderClient.getTraitsFrom(result).size(), is(3));
				assertThat(result.get(IndexConstants.FLD_LOCATION_ID), is("aekos.org.au/collection/loc1"));
				assertThat(result.get(IndexConstants.FLD_LOCATION_NAME), is("loc1"));
				assertThat(result.get(IndexConstants.FLD_DATASET_NAME), is("Some Dataset"));
				assertThat(result.get(IndexConstants.FLD_EVENT_DATE), is("2012-12-30"));
			}
		});
	}
	
	/**
	 * Can we add an environment record to the index?
	 */
	@Test
	public void testAddEnvRecord01() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				Set<String> environmentalVariableNames = new HashSet<>();
				environmentalVariableNames.add("var1");
				environmentalVariableNames.add("var2");
				EnvironmentLoaderRecord record = new EnvironmentLoaderRecord("loc123", environmentalVariableNames, "2012-12-30");
				objectUnderTest.addEnvRecord(record);
			}
		}, new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				int onlyAfterSingleDoc = 1;
				TopDocs td = searcher.search(new MatchAllDocsQuery(), onlyAfterSingleDoc);
				assertThat("Should be exactly one record", td.totalHits, is(1));
				Document result = searcher.doc(td.scoreDocs[0].doc);
				assertThat(result.get(IndexConstants.FLD_LOCATION_ID), is("loc123"));
				String[] resultTraits = result.getValues(IndexConstants.FLD_ENVIRONMENT);
				assertThat(resultTraits.length, is(2));
				assertThat(Arrays.asList(resultTraits), hasItems("var1", "var2"));
				assertThat(result.get(IndexConstants.FLD_EVENT_DATE), is("2012-12-30"));
			}
		});
	}
	
	/**
	 * Does the loader create species records so we find them when they contain a certain trait?
	 */
	@Test
	public void testIndexing01() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species1", 
						new HashSet<>(Arrays.asList(
								new Trait("basalArea", "100", "utils"), 
								new Trait("averageHeight", "1.3", "metres"))),
						"not important", "not important", "not important", "not important", "not important", "not important"));
				objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species2", 
						new HashSet<>(Arrays.asList(
								new Trait("lifeForm", "Forb", ""), 
								new Trait("averageHeight", "0.2", "metres"))),
						"not important", "not important", "not important", "not important", "not important", "not important"));
			}
		}, new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				Query q = SearchHelper.queryForSpeciesRecordTraitValue("averageHeight");
				int onlyAfterTotalHits = 1;
				TopDocs td = searcher.search(q, onlyAfterTotalHits);
				assertThat("Should find both species", td.totalHits, is(2));
				
			}
		});
	}
	
	/**
	 * Does the loader create species records so we find all available trait names and how many times they occur?
	 */
	@Test
	public void testIndexing02() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species1", 
						new HashSet<>(Arrays.asList(
								new Trait("basalArea", "100", "utils"), 
								new Trait("averageHeight", "1.3", "metres"))),
						"not important", "not important", "not important", "not important", "not important", "not important"));
				objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species2", 
						new HashSet<>(Arrays.asList(
								new Trait("lifeForm", "Forb", ""), 
								new Trait("averageHeight", "0.2", "metres"))),
						"not important", "not important", "not important", "not important", "not important", "not important"));
			}
		}, new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				FacetResult result = SearchHelper.facetSpeciesRecordTraits(searcher);
				assertThat("Should only have the 'trait' facet", result.dim, is(IndexConstants.FLD_TRAIT));
				assertThat("Should have expected facets in this order", Arrays.stream(result.labelValues)
						.map(e -> e.label + "|" + e.value)
						.collect(Collectors.toList()),
						is(Arrays.asList("averageHeight|2", "basalArea|1", "lifeForm|1")));
			}
		});
	}
	
	/**
	 * Does the loader create env records so we find them when they contain a certain variable?
	 */
	@Test
	public void testIndexing03() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				objectUnderTest.addEnvRecord(new EnvironmentLoaderRecord("loc1", new HashSet<String>(Arrays.asList("ph", "aspect")), "2012-12-30"));
				objectUnderTest.addEnvRecord(new EnvironmentLoaderRecord("loc2", new HashSet<String>(Arrays.asList("aspect", "basalArea")), "2012-12-30"));
			}
		}, new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				Query q = SearchHelper.queryForEnvRecordVariableValue("aspect");
				int onlyAfterTotalHits = 1;
				TopDocs td = searcher.search(q, onlyAfterTotalHits);
				assertThat("Should find both env records", td.totalHits, is(2));
			}
		});
	}
	
	/**
	 * Does the loader create env records so we find all available trait names and how many times they occur?
	 */
	@Test
	public void testIndexing04() throws Throwable {
		doLoadAndSearch(new LoadCallback() {
			@Override
			public void doLoad(LuceneLoaderClient objectUnderTest) throws IOException {
				objectUnderTest.addEnvRecord(new EnvironmentLoaderRecord("loc1", new HashSet<String>(Arrays.asList("envOne", "envTwo")), "2012-12-30"));
				objectUnderTest.addEnvRecord(new EnvironmentLoaderRecord("loc2", new HashSet<String>(Arrays.asList("envTwo", "envThree")), "2012-12-30"));
			}
		}, new DoSearchCallback() {
			@Override
			public void doSearch(IndexSearcher searcher) throws Throwable {
				FacetResult result = SearchHelper.facetEnvironmentRecordVariables(searcher);
				assertThat("Should only have the 'environment' facet", result.dim, is(IndexConstants.FLD_ENVIRONMENT));
				assertThat("Should have expected facets in this order", Arrays.stream(result.labelValues)
						.map(e -> e.label + "|" + e.value)
						.collect(Collectors.toList()),
						is(Arrays.asList("envTwo|2", "envOne|1", "envThree|1")));
			}
		});
	}
	
	// TODO add tests that verify that the written records can be found in all expected ways

	private static interface DoSearchCallback {
		void doSearch(IndexSearcher searcher) throws Throwable;
	}
	
	interface LoadCallback {
		void doLoad(LuceneLoaderClient objectUnderTest) throws IOException;
	}
	
	private void doLoadAndSearch(LoadCallback loadCallback, DoSearchCallback doSearchCallback) throws Throwable {
		LuceneLoaderClient objectUnderTest = new LuceneLoaderClient();
		TermIndexManager indexManager = new RAMDirectoryTermIndexManager();
		objectUnderTest.setIndexManager(indexManager);
		objectUnderTest.beginLoad();
		loadCallback.doLoad(objectUnderTest);
		objectUnderTest.endLoad();
		IndexSearcher indexSearcher = indexManager.getIndexSearcher();
		doSearchCallback.doSearch(indexSearcher);
		indexManager.releaseIndexSearcher(indexSearcher);
	}
}
