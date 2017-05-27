package au.org.aekos.api.producer.step;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class PropertyPathWithUnitsBagAttributeExtractor extends PropertyPathNoUnitsBagAttributeExtractor {

	private List<String> unitsPropertyPath;

	@Override
	protected String getUnits(Resource startingResource) {
		return followPath(startingResource, unitsPropertyPath);
	}

	public void setUnitsPropertyPath(String...pathFragments) {
		this.unitsPropertyPath = Arrays.asList(pathFragments);
	}
}
