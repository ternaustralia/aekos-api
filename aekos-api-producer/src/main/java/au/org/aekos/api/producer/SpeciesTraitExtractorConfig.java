package au.org.aekos.api.producer;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Dataset;

import au.org.aekos.api.producer.step.AttributeExtractor;
import au.org.aekos.api.producer.step.HardCodedUnitsAttributeExtractor;
import au.org.aekos.api.producer.step.InversePropertyDecoratedAttributeExtractor;
import au.org.aekos.api.producer.step.NoUnitsAttributeExtractor;
import au.org.aekos.api.producer.step.UnitsBasedAttributeExtractor;

public class SpeciesTraitExtractorConfig {

	private SpeciesTraitExtractorConfig() {}
	
	public static List<AttributeExtractor> getExtractors(ExtractionHelper extractionHelper, String propertyAndTypeNamespace,
			Dataset ds, String morphometricsLocalTypeName) {
		List<AttributeExtractor> result = new LinkedList<>();
		result.add(standardUnitBasedExtractor("averageHeight", extractionHelper));
		{
			HardCodedUnitsAttributeExtractor e = new HardCodedUnitsAttributeExtractor();
			e.setHelper(extractionHelper);
			e.setReferencingPropertyName("basalArea");
			e.setValuePropertyPath("value");
			e.setHardCodedUnits("square metres per hectare");
			result.add(e);
		}
		result.add(simpleNoUnitsExtractor("basalAreaFactor", extractionHelper));
		result.add(simpleNoUnitsExtractor("basalAreaCount", extractionHelper));
		result.add(standardUnitBasedExtractor("canopyCover", extractionHelper));
		result.add(standardUnitBasedExtractor("cover", extractionHelper));
		result.add(standardUnitBasedExtractor("height", extractionHelper));
		result.add(standardUnitBasedExtractor("biomass", extractionHelper));
		result.add(morphometricsExtractor("billLength", extractionHelper, propertyAndTypeNamespace, ds, morphometricsLocalTypeName));
		result.add(morphometricsExtractor("billLengthShield", extractionHelper, propertyAndTypeNamespace, ds, morphometricsLocalTypeName));
		result.add(morphometricsExtractor("billWidth", extractionHelper, propertyAndTypeNamespace, ds, morphometricsLocalTypeName));
		result.add(morphometricsExtractor("totalLength", extractionHelper, propertyAndTypeNamespace, ds, morphometricsLocalTypeName));
		result.add(morphometricsExtractor("weight", extractionHelper, propertyAndTypeNamespace, ds, morphometricsLocalTypeName));
		return result;
	}

	private static InversePropertyDecoratedAttributeExtractor morphometricsExtractor(String propertyName, ExtractionHelper extractionHelper,
			String propertyAndTypeNamespace, Dataset ds, String morphometricsLocalTypeName) {
		InversePropertyDecoratedAttributeExtractor e = new InversePropertyDecoratedAttributeExtractor();
		e.setDelegate(standardUnitBasedExtractor(propertyName, extractionHelper));
		e.setDs(ds);
		e.setPropertyAndTypeNamespace(propertyAndTypeNamespace);
		e.setInversePropertyLocalName("featureof");
		e.setTargetResourceTypeLocalName(morphometricsLocalTypeName);
		return e;
	}

	private static AttributeExtractor simpleNoUnitsExtractor(String referencingPropertyName, ExtractionHelper extractionHelper) {
		NoUnitsAttributeExtractor result = new NoUnitsAttributeExtractor();
		result.setHelper(extractionHelper);
		result.setReferencingPropertyName(referencingPropertyName);
		result.setValuePropertyPath("value");
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
