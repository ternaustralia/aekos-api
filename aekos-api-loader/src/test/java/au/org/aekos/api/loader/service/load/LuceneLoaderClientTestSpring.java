package au.org.aekos.api.loader.service.load;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
	 * Is the loader autowired correctly and able to load a record?
	 */
	@Test
	public void testAddSpeciesRecord01() throws Throwable {
		Set<Trait> traits = new HashSet<>();
		traits.add(new Trait("happiness", "100", "utils"));
		traits.add(new Trait("averageHeight", "1.3", "metres"));
		traits.add(new Trait("lifeForm", "Forb", ""));
		SpeciesLoaderRecord record = new SpeciesLoaderRecord("species1", traits, "not important", "not important",
				"not important", "not important", "not important", "not important");
		objectUnderTest.beginLoad();
		objectUnderTest.addSpeciesRecord(record);
		objectUnderTest.endLoad();
		Analyzer analyzer = new StandardAnalyzer();
		Query q = new BooleanQuery.Builder()
				.add(new QueryParser(IndexConstants.FLD_DOC_INDEX_TYPE, analyzer).parse(IndexConstants.DocTypes.SPECIES_RECORD), Occur.MUST)
				.add(new QueryParser(IndexConstants.FLD_SPECIES, analyzer).parse("\"species1\""), Occur.MUST)
				.build();
		int onlyAfterSingleDoc = 1;
		IndexSearcher searcher = indexMgr.getIndexSearcher();
		TopDocs td = searcher.search(q, onlyAfterSingleDoc);
		assertThat("Should be exactly one record", td.totalHits, is(1));
		Document result = searcher.doc(td.scoreDocs[0].doc);
		assertThat(result.get(IndexConstants.FLD_SPECIES), is("species1"));
		indexMgr.releaseIndexSearcher(searcher);
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