package au.org.aekos.api.producer.step.species;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class UnitsBasedTraitExtractor implements TraitExtractor {

	private ExtractionHelper helper;
	private String referencingPropertyName;

	@Override
	public TraitRecord doExtractOn(Resource subject, String parentId) {
		String name = referencingPropertyName;
		Resource traitSubject = helper.getResource(subject, referencingPropertyName);
		String value = helper.getLiteral(traitSubject, "value");
		Resource unitsSubject = helper.getResource(traitSubject, "units");
		String units = helper.getLiteral(unitsSubject, "name");;
		return new TraitRecord(parentId, name, value, units);
	}

	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}

	public void setReferencingPropertyName(String referencingPropertyName) {
		this.referencingPropertyName = referencingPropertyName;
	}
}
