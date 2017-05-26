package au.org.aekos.api.producer.step;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;

/**
 * Used to extract an attribute record that does not include units. You need to have a reference
 * to the parent subject (urn:parent) and know the target property (urn:referencingPropertyName).
 */
public class NoUnitsAttributeExtractor implements AttributeExtractor {

	private ExtractionHelper helper;
	private String referencingPropertyName;
	private List<String> valuePropertyPath;

	@Override
	public AttributeRecord doExtractOn(Resource subject) {
		PropertyPathNoUnitsBagAttributeExtractor r = new PropertyPathNoUnitsBagAttributeExtractor();
		r.setHelper(helper);
		r.setFinalName(referencingPropertyName);
		r.setValuePropertyPath(valuePropertyPath);
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

	public void setValuePropertyPath(String...pathFragments) {
		this.valuePropertyPath = Arrays.asList(pathFragments);
	}
}
