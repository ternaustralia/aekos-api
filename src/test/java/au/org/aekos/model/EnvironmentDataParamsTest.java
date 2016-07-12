package au.org.aekos.model;

import static au.org.aekos.TestUtils.loadMetric;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

public class EnvironmentDataParamsTest {

	/**
	 * Can we append ourself to the model?
	 */
	@Test
	public void testAppendTo01() {
		EnvironmentDataParams objectUnderTest = new EnvironmentDataParams(0, 20, Arrays.asList("Species One", "Species Two"), 
				Arrays.asList("Var One", "Var Two"));
		Model metricsModel = ModelFactory.createDefaultModel();
		Resource subject = metricsModel.createResource();
		objectUnderTest.appendTo(subject, metricsModel);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("EnvironmentDataParamsTest_testAppendTo01_expected.ttl"), out.toString());
	}
}