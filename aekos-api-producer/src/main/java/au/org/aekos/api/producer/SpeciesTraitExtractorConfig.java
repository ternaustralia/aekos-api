package au.org.aekos.api.producer;

import java.util.LinkedList;
import java.util.List;

import au.org.aekos.api.producer.step.AttributeExtractor;
import au.org.aekos.api.producer.step.NoUnitsAttributeExtractor;
import au.org.aekos.api.producer.step.UnitsBasedAttributeExtractor;

public class SpeciesTraitExtractorConfig {

	private SpeciesTraitExtractorConfig() {}
	
	public static List<AttributeExtractor> getExtractors(ExtractionHelper extractionHelper) {
		List<AttributeExtractor> result = new LinkedList<>();
		result.add(standardUnitBasedExtractor("averageHeight", extractionHelper));
		// FIXME check if 'basalArea' still doesn't have units in RDF. Create a hardcoded unit extractor for it
		{
			NoUnitsAttributeExtractor e = new NoUnitsAttributeExtractor();
			e.setHelper(extractionHelper);
			e.setReferencingPropertyName("basalAreaFactor");
			e.setValuePropertyPath("value");
			result.add(e);
		}
		result.add(standardUnitBasedExtractor("height", extractionHelper));
		result.add(standardUnitBasedExtractor("biomass", extractionHelper));
		return result;
	}

	private static AttributeExtractor standardUnitBasedExtractor(String referencingPropertyName, ExtractionHelper extractionHelper) {
		UnitsBasedAttributeExtractor result = new UnitsBasedAttributeExtractor();
		result.setHelper(extractionHelper);
		result.setReferencingPropertyName(referencingPropertyName);
		result.setValuePropertyPath("value");
		result.setUnitsPropertyPath("units", "name");
		return result;
	}
}
