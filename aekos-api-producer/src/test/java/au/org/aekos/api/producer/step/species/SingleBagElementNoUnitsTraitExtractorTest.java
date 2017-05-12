package au.org.aekos.api.producer.step.species;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.TestHelper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class SingleBagElementNoUnitsTraitExtractorTest {

	private static final String PROPERTY_NAMESPACE = "urn:";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we extract the values when they're present?
	 */
	@Test
	public void testDoExtractOn01() {
		SingleBagElementNoUnitsTraitExtractor objectUnderTest = new SingleBagElementNoUnitsTraitExtractor();
		objectUnderTest.setHelper(new ExtractionHelper(PROPERTY_NAMESPACE));
		objectUnderTest.setReferencingPropertyName("lifestage");
		objectUnderTest.setNestedPropertyName("commentary");
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource();
		h.addBag(subject, "lifestage", bag -> {
			h.addBagElement(bag, e -> {
				h.addLiteral(e, "commentary", "Mature Phase");
			});
		});
		TraitRecord result = objectUnderTest.doExtractOn(subject, "someParentId123");
		assertThat(result.getParentId(), is("\"someParentId123\""));
		assertThat(result.getName(), is("\"lifestage\""));
		assertThat(result.getValue(), is("\"Mature Phase\""));
		assertThat(result.getUnits(), is("\"\""));
	}
}
