package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.springframework.util.StreamUtils;

import au.org.aekos.model.SpeciesOccurrenceRecord;

public class JenaRetrievalServiceTest {

	/**
	 * Can we get all the records that are available for the specified species?
	 */
	@Test
	public void testGetSpeciesDataJson01() throws Throwable {
		JenaRetrievalService objectUnderTest = getObjectUnderTestWithDataAndQuery();
		List<SpeciesOccurrenceRecord> result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula"), 0);
		assertThat(result.size(), is(2));
	}

	/**
	 * Can we get all the records that are available for multiple species?
	 */
	@Test
	public void testGetSpeciesDataJson02() throws Throwable {
		JenaRetrievalService objectUnderTest = getObjectUnderTestWithDataAndQuery();
		List<SpeciesOccurrenceRecord> result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula", "Goodenia havilandii"), 0);
		assertThat(result.size(), is(3));
	}

	/**
	 * Can we limit the records that we get for the specified species?
	 */
	@Test
	public void testGetSpeciesDataJson03() throws Throwable {
		JenaRetrievalService objectUnderTest = getObjectUnderTestWithDataAndQuery();
		int onlyOne = 1;
		List<SpeciesOccurrenceRecord> result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula"), onlyOne);
		assertThat(result.size(), is(1));
	}
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is one value?
	 */
	@Test
	public void testGetProcessedSparql01() {
		List<String> s = Arrays.asList("Goodenia havilandii");
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SCIENTIFIC_NAME_PLACEHOLDER + " } }");
		String result = objectUnderTest.getProcessedSparql(s);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" } }"));
	}
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is more than one value?
	 */
	@Test
	public void testGetProcessedSparql02() {
		List<String> s = Arrays.asList("Goodenia havilandii", "Rosa canina");
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SCIENTIFIC_NAME_PLACEHOLDER + " } }");
		String result = objectUnderTest.getProcessedSparql(s);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" \"Rosa canina\" } }"));
	}
	
	private JenaRetrievalService getObjectUnderTestWithDataAndQuery() throws IOException {
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		Model darwinCoreGraph = ModelFactory.createDefaultModel();
		InputStream ttlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/test-darwin-core.ttl");
		darwinCoreGraph.read(ttlIS, null, "TTL");
		objectUnderTest.setDarwinCoreGraph(darwinCoreGraph);
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/darwin-core.rq");
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		objectUnderTest.setDarwinCoreQueryTemplate(out.toString());
		return objectUnderTest;
	}
}
