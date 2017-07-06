package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

public class SolutionVariableExtractorTest {

	/**
	 * Can we get the string value when it is present?
	 */
	@Test
	public void testGet01() {
		QuerySolution soln = getSolution("red", "colour");
		SolutionVariableExtractor objectUnderTest = new SolutionVariableExtractor(soln, "rdfSubject");
		String result = objectUnderTest.get("colour");
		assertThat(result, is("red"));
	}
	
	/**
	 * Can we handle when the target variable is NOT a literal?
	 */
	@Test
	public void testGet02() {
		QuerySolution soln = getSolution(new PopulateModelCallback() {
			@Override
			public void populateModel(Model m, Helper h) {
				m.add(h.resource("urn:sub1"), h.property("urn:linkedTo"), h.resource("urn:otherResource1"));
			}
		}, "SELECT ?oR WHERE { ?s ?p ?oR . }");
		SolutionVariableExtractor objectUnderTest = new SolutionVariableExtractor(soln, "rdfSubject");
		try {
			objectUnderTest.get("oR");
			fail();
		} catch (IllegalStateException e) {
			// success
		}
	}
	
	/**
	 * Can we get the optional value when it is present?
	 */
	@Test
	public void testGetOptional01() {
		QuerySolution soln = getSolution("blue", "colour");
		SolutionVariableExtractor objectUnderTest = new SolutionVariableExtractor(soln, "rdfSubject");
		String result = objectUnderTest.getOptional("colour");
		assertThat(result, is("blue"));
	}
	
	/**
	 * Can we handle when the optional value when it is NOT present?
	 */
	@Test
	public void testGetOptional02() {
		QuerySolution soln = getSolution("blue", "colour");
		SolutionVariableExtractor objectUnderTest = new SolutionVariableExtractor(soln, "rdfSubject");
		String result = objectUnderTest.getOptional("otherProperty");
		assertNull(result);
	}

	/**
	 * Can we handle when the target variable is NOT a literal?
	 */
	@Test
	public void testGetOptional03() {
		QuerySolution soln = getSolution(new PopulateModelCallback() {
			@Override
			public void populateModel(Model m, Helper h) {
				m.add(h.resource("urn:sub1"), h.property("urn:linkedTo"), h.resource("urn:otherResource1"));
			}
		}, "SELECT ?oR WHERE { ?s ?p ?oR . }");
		SolutionVariableExtractor objectUnderTest = new SolutionVariableExtractor(soln, "rdfSubject");
		try {
			objectUnderTest.getOptional("oR");
			fail();
		} catch (IllegalStateException e) {
			// success
		}
	}
	
	private QuerySolution getSolution(String value, String variableName) {
		String sparql = "SELECT * WHERE { ?s ?p ?" + variableName + " . }";
		return getSolution(new PopulateModelCallback() {
			@Override
			public void populateModel(Model m, Helper h) {
				m.add(h.resource("urn:sub1"), h.property("urn:value"), value);
			}
		}, sparql);
	}
	
	private QuerySolution getSolution(PopulateModelCallback callback, String sparql) {
		Dataset ds = DatasetFactory.create();
		Model m = ds.getDefaultModel();
		callback.populateModel(m, new Helper(m));
		Query query = QueryFactory.create(sparql);
		QueryExecution qexec = QueryExecutionFactory.create(query, ds);
		ResultSet theResults = qexec.execSelect();
		if (!theResults.hasNext()) {
			fail("No results");
		}
		return theResults.next();
	}
	
	interface PopulateModelCallback {
		void populateModel(Model m, Helper h);
	}
	
	private static class Helper {
		private final Model m;

		public Helper(Model m) {
			this.m = m;
		}
		
		public Resource resource(String uri) {
			return m.createResource(uri);
		}
		
		public Property property(String uri) {
			return m.createProperty(uri);
		}
	}
}
