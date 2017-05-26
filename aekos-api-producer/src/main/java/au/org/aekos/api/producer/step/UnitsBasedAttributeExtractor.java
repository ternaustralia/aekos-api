package au.org.aekos.api.producer.step;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;

/**
 * Used to extract an attribute record that includes units. You need to have a reference
 * to the parent subject (urn:parent) and know the target property (urn:referencingPropertyName).
 */
public class UnitsBasedAttributeExtractor implements AttributeExtractor {

	private ExtractionHelper helper;
	private String referencingPropertyName;
	private List<String> valuePropertyPath;
	private List<String> unitsPropertyPath;

	@Override
	public AttributeRecord doExtractOn(Resource subject) {
		PropertyPathWithUnitsBagAttributeExtractor r = new PropertyPathWithUnitsBagAttributeExtractor();
		r.setHelper(helper);
		r.setFinalName(referencingPropertyName);
		r.setValuePropertyPath(valuePropertyPath);
		r.setUnitsPropertyPath(unitsPropertyPath);
		Resource traitSubject = helper.getResource(subject, getLowerCaseLocalReferencingPropertyName());
		return r.doExtractOn(traitSubject);
	}
	
	@Override
	public String getId() {
		return referencingPropertyName;
	}
	
	@Override
	public boolean canHandle(Resource subject) {
		return helper.hasProperty(subject, getLowerCaseLocalReferencingPropertyName());
	}

	private String getLowerCaseLocalReferencingPropertyName() {
		return referencingPropertyName.toLowerCase();
	}
	
	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}

	public void setReferencingPropertyName(String referencingPropertyName) {
		this.referencingPropertyName = referencingPropertyName;
	}

	public void setValuePropertyPath(String...pathFragments) {
		this.valuePropertyPath = Arrays.asList(pathFragments);
	}

	public void setUnitsPropertyPath(String...pathFragments) {
		this.unitsPropertyPath = Arrays.asList(pathFragments);
	}
}
