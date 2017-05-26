package au.org.aekos.api.producer;

import java.util.LinkedList;
import java.util.List;

import au.org.aekos.api.producer.step.AttributeExtractor;
import au.org.aekos.api.producer.step.UnitsBasedAttributeExtractor;

public class SpeciesTraitExtractorConfig {

	private SpeciesTraitExtractorConfig() {}
	
	public static List<AttributeExtractor> getExtractors(ExtractionHelper extractionHelper) {
		List<AttributeExtractor> result = new LinkedList<>();
		result.add(unitBasedExtractor("averageHeight", extractionHelper));
		// FIXME check if 'basalArea' still doesn't have units in RDF. Create a hardcoded unit extractor for it
		result.add(unitBasedExtractor("height", extractionHelper));
		result.add(unitBasedExtractor("biomass", extractionHelper));
		return result;
	}

	private static AttributeExtractor unitBasedExtractor(String referencingPropertyName, ExtractionHelper extractionHelper) {
		UnitsBasedAttributeExtractor result = new UnitsBasedAttributeExtractor();
		result.setHelper(extractionHelper);
		result.setReferencingPropertyName(referencingPropertyName);
		return result;
	}
}
