package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=LuceneLoaderClientTestSpringContext.class)
public class LuceneLoaderClientTestSpring {

	@Autowired
	private LuceneLoaderClient objectUnderTest;
	
	@Autowired
	private TermIndexManager indexMgr;
	
	/**
	 * Can we add a species record to the index?
	 */
	@Test
	public void testAddSpeciesRecord01() throws Throwable {
		Set<Trait> traits = new HashSet<>();
		traits.add(new Trait("happiness", "100", "utils"));
		traits.add(new Trait("averageHeight", "1.3", "metres"));
		traits.add(new Trait("lifeForm", "Forb", ""));
		SpeciesLoaderRecord record = new SpeciesLoaderRecord("species1", traits, "sampling protocol #1", "use it for whatever");
		objectUnderTest.beginLoad();
		objectUnderTest.addSpeciesRecord(record);
		objectUnderTest.endLoad();
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_SPECIES, analyzer).parse("\"species1\""), Occur.MUST)
				.build();
		int onlyAfterSingleDoc = 1;
		doSearch(new DoSearchCallback() {
			@Override
			public void handle(IndexSearcher searcher) throws Throwable {
				TopDocs td = searcher.search(q, onlyAfterSingleDoc);
				assertThat("Should be exactly one record", td.totalHits, is(1));
				Document result = searcher.doc(td.scoreDocs[0].doc);
				assertThat(result.get(IndexConstants.FLD_SPECIES), is("species1"));
				assertThat(result.get(IndexConstants.FLD_SAMPLING_PROTOCOL), is("sampling protocol #1"));
				assertThat(result.get(IndexConstants.FLD_BIBLIOGRAPHIC_CITATION), is("use it for whatever"));
				String[] resultTraits = result.getValues(IndexConstants.FLD_TRAIT);
				assertThat(resultTraits.length, is(3));
				assertThat(Arrays.asList(resultTraits), hasItems("happiness","averageHeight","lifeForm"));
				assertThat(LuceneLoaderClient.getTraitsFrom(result).size(), is(3));
			}
		});
	}
	
	/**
	 * Can we find species records when they contain a certain trait?
	 */
	@Test
	public void testIndexing01() throws Throwable {
		objectUnderTest.beginLoad();
		objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species1", 
				new HashSet<>(Arrays.asList(
						new Trait("happiness", "100", "utils"), 
						new Trait("averageHeight", "1.3", "metres"))),
				"sampling protocol #1", "use it for whatever"));
		objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species2", 
				new HashSet<>(Arrays.asList(
						new Trait("lifeForm", "Forb", ""), 
						new Trait("averageHeight", "0.2", "metres"))),
				"sampling protocol #1", "use it for whatever"));
		objectUnderTest.endLoad();
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_TRAIT, analyzer).parse("\"averageHeight\""), Occur.MUST)
				.build();
		int onlyAfterSingleDoc = 1;
		doSearch(new DoSearchCallback() {
			@Override
			public void handle(IndexSearcher searcher) throws Throwable {
				TopDocs td = searcher.search(q, onlyAfterSingleDoc);
				assertThat("Should find both species", td.totalHits, is(2));
			}
		});
	}
	
	/**
	 * Can we find all available trait names?
	 */
	@Test
	public void testIndexing02() throws Throwable {
		objectUnderTest.beginLoad();
		objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species1", 
				new HashSet<>(Arrays.asList(
						new Trait("happiness", "100", "utils"), 
						new Trait("averageHeight", "1.3", "metres"))),
				"sampling protocol #1", "use it for whatever"));
		objectUnderTest.addSpeciesRecord(new SpeciesLoaderRecord("species2", 
				new HashSet<>(Arrays.asList(
						new Trait("lifeForm", "Forb", ""), 
						new Trait("averageHeight", "0.2", "metres"))),
				"sampling protocol #1", "use it for whatever"));
		objectUnderTest.endLoad();
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser qp = new QueryParser(IndexConstants.FLD_TRAIT, analyzer);
		qp.setAllowLeadingWildcard(true);
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(qp.parse("*"), Occur.MUST)
				.build();
		doSearch(new DoSearchCallback() {
			@Override
			public void handle(IndexSearcher searcher) throws Throwable {
				TopDocs td = searcher.search(q, 3);
				Set<String> resultTraits = Arrays.stream(td.scoreDocs).map(e -> {
					try {
						return searcher.doc(e.doc).getValues(IndexConstants.FLD_TRAIT);
					} catch (IOException e1) { throw new RuntimeException(e1); }
				})
				.flatMap(e -> Arrays.stream(e))
				.collect(Collectors.toSet());
				// FIXME get this faceting working
			    SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());
				FacetsCollector fc = new FacetsCollector();
				searcher.search(q, fc);
				SortedSetDocValuesFacetCounts facets = new SortedSetDocValuesFacetCounts(state, fc);
				System.out.println(facets.getTopChildren(10, IndexConstants.FLD_TRAIT));
				
				
				assertThat("Should find both docs", td.totalHits, is(2));
				assertThat("Should find all traits but only found: " + resultTraits, resultTraits.size(), is(3));
			}
		});
	}
	
	// TODO add a test that verifies that the written records can be found in all expected ways
	
	/**
	 * Can we add an environment record to the index?
	 */
	@Test
	public void testAddEnvRecord01() throws Throwable {
		Set<String> environmentalVariableNames = new HashSet<>();
		environmentalVariableNames.add("var1");
		environmentalVariableNames.add("var2");
		EnvironmentLoaderRecord record = new EnvironmentLoaderRecord("loc123", environmentalVariableNames);
		objectUnderTest.beginLoad();
		objectUnderTest.addEnvRecord(record);
		objectUnderTest.endLoad();
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.ENV_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_LOCATION_ID, analyzer).parse("\"loc123\""), Occur.MUST)
				.build();
		int onlyAfterSingleDoc = 1;
		doSearch(new DoSearchCallback() {
			@Override
			public void handle(IndexSearcher searcher) throws Throwable {
				TopDocs td = searcher.search(q, onlyAfterSingleDoc);
				assertThat("Should be exactly one record", td.totalHits, is(1));
				Document result = searcher.doc(td.scoreDocs[0].doc);
				assertThat(result.get(IndexConstants.FLD_LOCATION_ID), is("loc123"));
				String[] resultTraits = result.getValues(IndexConstants.FLD_ENVIRONMENT);
				assertThat(resultTraits.length, is(2));
				assertThat(Arrays.asList(resultTraits), hasItems("var1", "var2"));
			}
		});
	}
	
	private void doSearch(DoSearchCallback doSearchCallback) throws Throwable {
		IndexSearcher searcher = indexMgr.getIndexSearcher();
		doSearchCallback.handle(searcher);
		indexMgr.releaseIndexSearcher(searcher);
	}

	private static interface DoSearchCallback {
		void handle(IndexSearcher searcher) throws Throwable;
	}
}

@Configuration
@ComponentScan(
	basePackages={
		"au.org.aekos.api.loader.service.index",
		"au.org.aekos.api.loader.service.load"})
class LuceneLoaderClientTestSpringContext {
	
	@Bean
	public TermIndexManager /*override the bean from the component scan*/FSDirectoryTermIndexManager() {
		return new RAMDirectoryTermIndexManager();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
	    PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
	    Properties properties = new Properties();
	    properties.setProperty("lucene.index.writer.commitLimit", "1000");
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