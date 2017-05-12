package au.org.aekos.api.producer.step.species;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class SingleBagElementNoUnitsTraitExtractor implements TraitExtractor {

	private static final String NO_UNITS = "";
	private ExtractionHelper helper;
	private String referencingPropertyName;
	private String nestedPropertyName;

	@Override
	public TraitRecord doExtractOn(Resource subject, String parentId) {
		String name = referencingPropertyName;
		Resource bag = helper.getResource(subject, referencingPropertyName);
		Resource firstElement = bag.as(Bag.class).iterator().next().asResource();
		String value = helper.getLiteral(firstElement, nestedPropertyName);
		return new TraitRecord(parentId, name, value, NO_UNITS);
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

	public void setNestedPropertyName(String nestedPropertyName) {
		this.nestedPropertyName = nestedPropertyName;
	}
}
