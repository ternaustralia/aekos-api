package au.org.aekos.api.producer.step;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.TestHelper;
import au.org.aekos.api.producer.step.AttributeRecord;
import au.org.aekos.api.producer.step.MissingDataException;

public class PropertyPathNoUnitsBagAttributeExtractorTest {

	private static final String PROPERTY_NAMESPACE = "urn:";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we tell when we can handle a type?
	 */
	@Test
	public void testCanHandle01() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathNoUnitsBagAttributeExtractor objectUnderTest = new PropertyPathNoUnitsBagAttributeExtractor();
		objectUnderTest.setTargetTypeLocalName("DISTURBANCEEVIDENCE");
		Resource type = m.createResource("urn:DISTURBANCEEVIDENCE");
		Resource subject = m.createResource("urn:someSub1", type);
		boolean result = objectUnderTest.canHandle(subject);
		assertTrue("Should be able to handle this type", result);
	}
	
	/**
	 * Can we tell when we CANNOT handle a type?
	 */
	@Test
	public void testCanHandle02() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathNoUnitsBagAttributeExtractor objectUnderTest = new PropertyPathNoUnitsBagAttributeExtractor();
		objectUnderTest.setTargetTypeLocalName("DISTURBANCEEVIDENCE");
		Resource type = m.createResource("urn:SOMEOTHERTYPE");
		Resource subject = m.createResource("urn:someSub1", type);
		boolean result = objectUnderTest.canHandle(subject);
		assertFalse("Should NOT be able to handle this type", result);
	}
	
	/**
	 * Can we handle a resource with no type?
	 */
	@Test
	public void testCanHandle03() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathNoUnitsBagAttributeExtractor objectUnderTest = new PropertyPathNoUnitsBagAttributeExtractor();
		objectUnderTest.setTargetTypeLocalName("DISTURBANCEEVIDENCE");
		Resource subjectWithoutType = m.createResource("urn:someSub1");
		boolean result = objectUnderTest.canHandle(subjectWithoutType);
		assertFalse("Should NOT be able to handle something with no type", result);
	}
	
	/**
	 * Can we extract the trait when the data exists?
	 */
	@Test
	public void testDoExtractOn01() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathNoUnitsBagAttributeExtractor objectUnderTest = new PropertyPathNoUnitsBagAttributeExtractor();
		objectUnderTest.setFinalName("disturbanceEvidence");
		objectUnderTest.setHelper(new ExtractionHelper("urn:"));
		objectUnderTest.setValuePropertyPath(Arrays.asList("disturbancetype", "commentary"));
		Resource subject = m.createResource("urn:someSub1");
		h.addResource(subject, "disturbancetype", r -> {
			h.addLiteral(r, "commentary", "none");
		});
		AttributeRecord result = objectUnderTest.doExtractOn(subject);
		assertThat(result.getName(), is("disturbanceEvidence"));
		assertThat(result.getValue(), is("none"));
		assertNull(result.getUnits());
	}
	
	/**
	 * Can we report a useful error when the path is wrong (missing the secondLevel)?
	 */
	@Test
	public void testDoExtractOn02() {
		Model m = ModelFactory.createDefaultModel();
		PropertyPathNoUnitsBagAttributeExtractor objectUnderTest = new PropertyPathNoUnitsBagAttributeExtractor();
		objectUnderTest.setFinalName("disturbanceEvidence");
		objectUnderTest.setHelper(new ExtractionHelper("urn:"));
		objectUnderTest.setValuePropertyPath(Arrays.asList("disturbancetype", "commentary"));
		Resource subject = m.createResource("urn:someSub1");
		h.addResource(subject, "disturbancetype", r -> {
			h.addResource(r, "secondLevel", r2 -> {
				h.addLiteral(r2, "commentary", "none");
			});
		});
		try {
			objectUnderTest.doExtractOn(subject);
			fail();
		} catch (MissingDataException e) {
			// success
		}
	}
}
