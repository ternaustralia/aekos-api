package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.TestHelper;

public class UnitsBasedAttributeExtractorTest {

	private static final String PROPERTY_NAMESPACE = "urn:";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we extract the values when they're present?
	 */
	@Test
	public void testDoExtractOn01() {
		UnitsBasedAttributeExtractor objectUnderTest = new UnitsBasedAttributeExtractor();
		objectUnderTest.setHelper(new ExtractionHelper(PROPERTY_NAMESPACE));
		objectUnderTest.setReferencingPropertyName("height");
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource();
		h.addResource(subject, "height", r -> {
			h.addLiteral(r, "value", "2.3");
			h.addResource(r, "units", r1 -> {
				h.addLiteral(r1, "name", "metres");
			});
		});
		AttributeRecord result = objectUnderTest.doExtractOn(subject);
		assertThat(result.getName(), is("height"));
		assertThat(result.getValue(), is("2.3"));
		assertThat(result.getUnits(), is("metres"));
	}
}
