package au.org.aekos.api.producer;

import java.util.LinkedList;
import java.util.List;

import au.org.aekos.api.producer.step.BagAttributeExtractor;
import au.org.aekos.api.producer.step.PropertyPathNoUnitsBagAttributeExtractor;
import au.org.aekos.api.producer.step.PropertyPathWithUnitsBagAttributeExtractor;

public class EnvironmentVariableExtractorConfig {

	private EnvironmentVariableExtractorConfig() {}
	
	public static List<BagAttributeExtractor> getExtractors(ExtractionHelper extractionHelper) {
		List<BagAttributeExtractor> result = new LinkedList<>();
		result.add(categoryNoUnitsExtractor("latestLandUse", "SAMPLEDAREA", extractionHelper));
		result.add(commentaryNoUnitsExtractor("weatherComment", "weather", "SAMPLEDAREA", extractionHelper));
		result.add(standardUnitBasedExtractor("aspect", "LANDSCAPE", extractionHelper));
		result.add(standardUnitBasedExtractor("slope", "LANDSCAPE", extractionHelper));
		addCommentaryAndCategoryNoUnitsExtractors(result, "soilType", "SOIL", extractionHelper);
		addCommentaryAndCategoryNoUnitsExtractors(result, "soilTexture", "SOIL", extractionHelper);
		addCommentaryAndCategoryNoUnitsExtractors(result, "surfaceType", "SOIL", extractionHelper);
		result.add(standardUnitBasedExtractor("electricalConductivity", "SOIL", extractionHelper));
		// TODO make hardcoded units for pH
		result.add(standardUnitBasedExtractor("totalOrganicCarbon", "SOIL", extractionHelper));
		result.add(standardUnitBasedExtractor("clay", "SOIL", extractionHelper));
		result.add(standardUnitBasedExtractor("silt", "SOIL", extractionHelper));
		result.add(standardUnitBasedExtractor("sand", "SOIL", extractionHelper));
		{
			PropertyPathNoUnitsBagAttributeExtractor e = new PropertyPathNoUnitsBagAttributeExtractor();
			e.setFinalName("visibleFireEvidence");
			e.setHelper(extractionHelper);
			e.setTargetTypeLocalName("FIREEVIDENCE");
			e.setValuePropertyPath("visiblefireevidence");
			result.add(e);
		}
		addCommentaryAndCategoryNoUnitsExtractors(result, "disturbanceType", "DISTURBANCEEVIDENCE", extractionHelper);
		result.add(standardUnitBasedExtractor("disturbanceCover", "DISTURBANCEEVIDENCE", extractionHelper));
		addCommentaryAndCategoryNoUnitsExtractors(result, "erosionType", "EROSIONEVIDENCE", extractionHelper);
		addCommentaryAndCategoryNoUnitsExtractors(result, "erosionState", "EROSIONEVIDENCE", extractionHelper);
		return result;
	}

	private static void addCommentaryAndCategoryNoUnitsExtractors(List<BagAttributeExtractor> result, String finalNameAndReferencingPropertyName,
			String targetTypeLocalName, ExtractionHelper extractionHelper) {
		result.add(categoryNoUnitsExtractor(finalNameAndReferencingPropertyName, targetTypeLocalName, extractionHelper));
		result.add(commentaryNoUnitsExtractor(finalNameAndReferencingPropertyName, targetTypeLocalName, extractionHelper));
	}

	private static BagAttributeExtractor commentaryNoUnitsExtractor(String finalName, String referencingPropertyName, String targetTypeLocalName,
			ExtractionHelper extractionHelper) {
		PropertyPathNoUnitsBagAttributeExtractor result = new PropertyPathNoUnitsBagAttributeExtractor();
		result.setFinalName(finalName);
		result.setHelper(extractionHelper);
		result.setTargetTypeLocalName(targetTypeLocalName);
		result.setValuePropertyPath(referencingPropertyName.toLowerCase(), "category", "name");
		return result;
	}

	private static BagAttributeExtractor categoryNoUnitsExtractor(String finalNameAndReferencingPropertyName, String targetTypeLocalName,
			ExtractionHelper extractionHelper) {
		return commentaryNoUnitsExtractor(finalNameAndReferencingPropertyName, finalNameAndReferencingPropertyName, targetTypeLocalName, extractionHelper);
	}
	
	private static BagAttributeExtractor commentaryNoUnitsExtractor(String finalNameAndReferencingPropertyName, String targetTypeLocalName,
			ExtractionHelper extractionHelper) {
		PropertyPathNoUnitsBagAttributeExtractor result = new PropertyPathNoUnitsBagAttributeExtractor();
		result.setFinalName(finalNameAndReferencingPropertyName);
		result.setHelper(extractionHelper);
		result.setTargetTypeLocalName(targetTypeLocalName);
		result.setValuePropertyPath(finalNameAndReferencingPropertyName.toLowerCase(), "commentary");
		return result;
	}

	private static BagAttributeExtractor standardUnitBasedExtractor(String finalNameAndReferencingPropertyName, String targetTypeLocalName,
			ExtractionHelper extractionHelper) {
		PropertyPathWithUnitsBagAttributeExtractor result = new PropertyPathWithUnitsBagAttributeExtractor();
		result.setFinalName(finalNameAndReferencingPropertyName);
		result.setHelper(extractionHelper);
		result.setTargetTypeLocalName(targetTypeLocalName);
		result.setValuePropertyPath(finalNameAndReferencingPropertyName.toLowerCase(), "value");
		result.setUnitsPropertyPath(finalNameAndReferencingPropertyName.toLowerCase(), "units", "name");
		return result;
	}
}
