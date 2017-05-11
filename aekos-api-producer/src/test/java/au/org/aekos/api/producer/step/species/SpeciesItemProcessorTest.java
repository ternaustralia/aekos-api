package au.org.aekos.api.producer.step.species;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.TestHelper;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class SpeciesItemProcessorTest {

	private static final String SPECIES_RECORD_RDF_SUBJECT = "speciesRecordRdfSubject";
	private static final String SPECIES_RECORD_RDF_GRAPH = "speciesRecordRdfGraph";
	private static final String TEST_PROJECT_NAMESPACE = "urn:";
	private static final String PROPERTY_NAMESPACE = "urn:";
	private static final String COMMON_GRAPH = "commonGraphName";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);

	/**
	 * Can we process a record when the data is available in the dataset?
	 */
	@Test
	public void testProcess01() throws Throwable {
		SpeciesItemProcessor objectUnderTest = new SpeciesItemProcessor();
		Dataset ds = DatasetFactory.create();
		Map<String, String> dataFromRdf = testProcess01_populate(ds);
		objectUnderTest.setExtractors(Arrays.asList(unitExtractor("height", ds.getNamedModel(dataFromRdf.get(COMMON_GRAPH)))));
		objectUnderTest.setDataset(ds);
		InputSpeciesRecord item = new InputSpeciesRecord("dab27d3c-5884-4de2-bf74-9dc73f874496", dataFromRdf.get(SPECIES_RECORD_RDF_SUBJECT), 
				dataFromRdf.get(SPECIES_RECORD_RDF_GRAPH), 1, "aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002", "Eucalyptus obliqua", null);
		OutputSpeciesWrapper result = objectUnderTest.process(item);
		List<TraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		assertThat(traits.get(0).getName(), is("\"height\""));
	}

	private TraitExtractor unitExtractor(String referencingPropertyName, Model commonGraph) {
		UnitsBasedTraitExtractor result = new UnitsBasedTraitExtractor();
		ExtractionHelper helper = new ExtractionHelper(PROPERTY_NAMESPACE);
		helper.setCommonGraph(commonGraph);
		result.setHelper(helper);
		result.setReferencingPropertyName(referencingPropertyName);
		return result;
	}

	private Map<String, String> testProcess01_populate(Dataset ds) {
		Map<String, String> result = new HashMap<>();
		// Common graph
		String commonGraphName = TEST_PROJECT_NAMESPACE + "test_common#";
		Model commonModel = ds.getNamedModel(commonGraphName);
		Resource measurementUnitSubject = commonModel.createResource(commonGraphName + "mu1");
		h.addLiteral(measurementUnitSubject, "name", "metres");
		// Project related graph
		String projectGraphName = TEST_PROJECT_NAMESPACE + "test_project#";
		Model model = ds.getNamedModel(projectGraphName);
		String speciesSubjectUri = TEST_PROJECT_NAMESPACE + "INDIVIDUALORGANISM-T1493794712603";
		Resource speciesSubject = model.createResource(speciesSubjectUri);
		h.addResource(speciesSubject, "height", r -> {
			h.addLiteral(r, "value", "2.3");
			h.addResourceReference(r, "units", measurementUnitSubject);
		});
//		addTrait(model, speciesSubject, "lifeForm", "Shrub", "");
//		addTrait(model, speciesSubject, "lifeStage", "Plant is Dead = true", "");
		result.put(COMMON_GRAPH, commonGraphName);
		result.put(SPECIES_RECORD_RDF_GRAPH, projectGraphName);
		result.put(SPECIES_RECORD_RDF_SUBJECT, speciesSubjectUri);
		return result;
	}
}
