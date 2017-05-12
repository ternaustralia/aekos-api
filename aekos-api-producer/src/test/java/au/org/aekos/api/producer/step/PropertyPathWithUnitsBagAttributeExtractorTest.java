package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.TestHelper;

public class PropertyPathWithUnitsBagAttributeExtractorTest {

	private static final String PROPERTY_NAMESPACE = "urn:";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we extract the trait when the data exists?
	 */
	@Test
	public void testDoExtractOn01() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathWithUnitsBagAttributeExtractor objectUnderTest = new PropertyPathWithUnitsBagAttributeExtractor();
		objectUnderTest.setFinalName("slope");
		objectUnderTest.setHelper(new ExtractionHelper("urn:"));
		objectUnderTest.setValuePropertyPath(Arrays.asList("slope", "value"));
		objectUnderTest.setUnitsPropertyPath(Arrays.asList("slope", "units", "name"));
		Resource subject = m.createResource("urn:someSub1");
		h.addResource(subject, "slope", r -> {
			h.addLiteral(r, "value", "4");
			h.addResource(r, "units", r2 -> {
				h.addLiteral(r2, "name", "degrees");
			});
		});
		AttributeRecord result = objectUnderTest.doExtractOn(subject, "parent123");
		assertThat(result.getParentId(), is("\"parent123\""));
		assertThat(result.getName(), is("\"slope\""));
		assertThat(result.getValue(), is("\"4\""));
		assertThat(result.getUnits(), is("\"degrees\""));
	}
}
