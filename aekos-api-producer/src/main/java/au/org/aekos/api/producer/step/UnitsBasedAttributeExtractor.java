package au.org.aekos.api.producer.step;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;

public class UnitsBasedAttributeExtractor implements AttributeExtractor {

	private ExtractionHelper helper;
	private String referencingPropertyName;

	@Override
	public AttributeRecord doExtractOn(Resource subject, String parentId) {
		String name = referencingPropertyName;
		Resource traitSubject = helper.getResource(subject, referencingPropertyName);
		String value = helper.getLiteral(traitSubject, "value");
		Resource unitsSubject = helper.getResource(traitSubject, "units");
		String units = helper.getLiteral(unitsSubject, "name");;
		return new AttributeRecord(parentId, name, value, units);
	}
	
	@Override
	public String getId() {
		return referencingPropertyName;
	}

	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}

	public void setReferencingPropertyName(String referencingPropertyName) {
		this.referencingPropertyName = referencingPropertyName;
	}
}
