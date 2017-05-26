package au.org.aekos.api.producer.step;

import java.util.Arrays;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;

/**
 * Used to extract an attribute record that includes units. You need to have a reference
 * to the parent subject (urn:parent) and know the target property (urn:referencingPropertyName).
 * 
 * Has a predefined knowledge of how units-based records are stored and expect the target to match.
 */
public class UnitsBasedAttributeExtractor implements AttributeExtractor {

	private ExtractionHelper helper;
	private String referencingPropertyName;

	@Override
	public AttributeRecord doExtractOn(Resource subject) {
		PropertyPathWithUnitsBagAttributeExtractor r = new PropertyPathWithUnitsBagAttributeExtractor();
		r.setHelper(helper);
		r.setFinalName(referencingPropertyName);
		r.setValuePropertyPath(Arrays.asList("value"));
		r.setUnitsPropertyPath(Arrays.asList("units", "name"));
		Resource traitSubject = helper.getResource(subject, referencingPropertyName);
		return r.doExtractOn(traitSubject);
	}
	
	@Override
	public String getId() {
		return referencingPropertyName;
	}
	
	@Override
	public boolean canHandle(Resource subject) {
		return helper.hasProperty(subject, referencingPropertyName);
	}

	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}

	public void setReferencingPropertyName(String referencingPropertyName) {
		this.referencingPropertyName = referencingPropertyName;
	}
}
