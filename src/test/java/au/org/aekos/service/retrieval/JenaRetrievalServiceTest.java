package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class JenaRetrievalServiceTest {
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is one value?
	 */
	@Test
	public void testGetProcessedSparql01() {
		List<String> s = Arrays.asList("Goodenia havilandii");
		int start = 0;
		int rows = 20;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SCIENTIFIC_NAME_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" }"
				+ " OFFSET 0 LIMIT 20 }"));
	}
	
	/**
	 * Can we substitute the scientificNames value list into the SPARQL query when there is more than one value?
	 */
	@Test
	public void testGetProcessedSparql02() {
		List<String> s = Arrays.asList("Goodenia havilandii", "Rosa canina");
		int start = 5000;
		int rows = 100;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SCIENTIFIC_NAME_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Goodenia havilandii\" \"Rosa canina\" }"
				+ " OFFSET 5000 LIMIT 100 }"));
	}
	
	/**
	 * Do we retrieve all rows (or at least a lot) when the limit is 0?
	 */
	@Test
	public void testGetProcessedSparql03() {
		List<String> s = Arrays.asList("Rosa canina");
		int start = 0;
		int rows = 0;
		JenaRetrievalService objectUnderTest = new JenaRetrievalService();
		objectUnderTest.setDarwinCoreQueryTemplate("SELECT * WHERE {?s ?p ?o . VALUES ?s { " + JenaRetrievalService.SCIENTIFIC_NAME_PLACEHOLDER + " }"
				+ " OFFSET %OFFSET_PLACEHOLDER% LIMIT %LIMIT_PLACEHOLDER% }");
		String result = objectUnderTest.getProcessedDarwinCoreSparql(s, start, rows);
		assertThat(result, is("SELECT * WHERE {?s ?p ?o . VALUES ?s { \"Rosa canina\" }"
				+ " OFFSET 0 LIMIT " + Integer.MAX_VALUE + " }"));
	}
}
