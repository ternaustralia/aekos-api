package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

public class AbstractRdfReaderTest {

	/**
	 * Does a thrown exception give the expected information?
	 */
	@Test
	public void testRead01() {
		ConcreteRdfReader objectUnderTest = new ConcreteRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getDefaultModel();
		addSomeData(m);
		objectUnderTest.setDs(ds);
		try {
			objectUnderTest.read();
			fail();
		} catch (Exception e) {
			String msg = e.getMessage();
			assertThat(msg, is("Failed while processing a solution. Available values: [p=urn:has_value, o=value1, s=urn:thing1]"));
		}
	}

	private void addSomeData(Model m) {
		m.add(resource("urn:thing1", m), prop("urn:has_value", m), "value1");
		m.add(resource("urn:thing2", m), prop("urn:other_property", m), "value2");
	}

	private Resource resource(String uri, Model m) {
		return m.createResource(uri);
	}
	
	private Property prop(String uri, Model m) {
		return m.createProperty(uri);
	}
}

class ConcreteRdfReader extends AbstractRdfReader<String> {

	@Override
	public String mapSolution(QuerySolution solution) {
		if (!solution.contains("urn:has_value")) {
			throw new RuntimeException();
		}
		return "doesn't matter";
	}

	@Override public String getRecordTypeName() { return null; }
	
	@Override public String getSparqlQuery() {
		return "SELECT * WHERE { ?s ?p ?o . }";
	}
}
