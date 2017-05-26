package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
	
	/**
	 * Can we tell that we can handle a subject because the property exists (we don't check deeper than that)?
	 */
	@Test
	public void testCanHandle01() {
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource("urn:someSub1");
		h.addLiteral(subject, "height", "value doesn't matter, just need a triple to exist");
		UnitsBasedAttributeExtractor objectUnderTest = new UnitsBasedAttributeExtractor();
		objectUnderTest.setHelper(new ExtractionHelper(PROPERTY_NAMESPACE));
		objectUnderTest.setReferencingPropertyName("height");
		boolean result = objectUnderTest.canHandle(subject);
		assertTrue("Should be able to handle it, the property exists", result);
	}
	
	/**
	 * Can we tell that we can NOT handle a subject because the property DOES NOT exist?
	 */
	@Test
	public void testCanHandle02() {
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource("urn:someSub1");
		h.addLiteral(subject, "basalArea", "value doesn't matter, just need a triple to exist");
		UnitsBasedAttributeExtractor objectUnderTest = new UnitsBasedAttributeExtractor();
		objectUnderTest.setHelper(new ExtractionHelper(PROPERTY_NAMESPACE));
		objectUnderTest.setReferencingPropertyName("height");
		boolean result = objectUnderTest.canHandle(subject);
		assertFalse("Should NOT be able to handle it, the property DOES NOT exist", result);
	}
}
