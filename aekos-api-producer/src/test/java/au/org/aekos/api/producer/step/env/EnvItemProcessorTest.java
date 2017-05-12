package au.org.aekos.api.producer.step.env;

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
import au.org.aekos.api.producer.step.AttributeRecord;
import au.org.aekos.api.producer.step.BagAttributeExtractor;
import au.org.aekos.api.producer.step.PropertyPathNoUnitsBagAttributeExtractor;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;

public class EnvItemProcessorTest {

	private static final String ENV_RECORD_RDF_SUBJECT = "envRecordRdfSubject";
	private static final String ENV_RECORD_RDF_GRAPH = "envRecordRdfGraph";
	private static final String TEST_PROJECT_NAMESPACE = "urn:";
	private static final String PROPERTY_NAMESPACE = "urn:";
	private static final String COMMON_GRAPH = "commonGraphName";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	private final ExtractionHelper helper = new ExtractionHelper(PROPERTY_NAMESPACE);

	/**
	 * Can we process a record when the data is available in the dataset?
	 */
	@Test
	public void testProcess01() throws Throwable {
		EnvItemProcessor objectUnderTest = new EnvItemProcessor();
		Dataset ds = DatasetFactory.create();
		Map<String, String> dataFromRdf = testProcess01_populate(ds);
		helper.setCommonGraph(ds.getNamedModel(dataFromRdf.get(COMMON_GRAPH)));
		objectUnderTest.setExtractors(Arrays.asList(propPathExtractor()));
		objectUnderTest.setDataset(ds);
		objectUnderTest.setHelper(helper);
		InputEnvRecord item = new InputEnvRecord("aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002", 0, 0, "GDA94",
				"Null island", "not/important", dataFromRdf.get(ENV_RECORD_RDF_SUBJECT), dataFromRdf.get(ENV_RECORD_RDF_GRAPH));
		OutputEnvWrapper result = objectUnderTest.process(item);
		List<AttributeRecord> variables = result.getEnvVarRecords();
		assertThat(variables.size(), is(1));
		assertThat(variables.get(0).getName(), is("\"disturbanceEvidence\""));
		assertThat(variables.get(0).getValue(), is("\"the value!\""));
	}

	private BagAttributeExtractor propPathExtractor() {
		PropertyPathNoUnitsBagAttributeExtractor result = new PropertyPathNoUnitsBagAttributeExtractor();
		result.setHelper(helper);
		result.setFinalName("disturbanceEvidence");
		result.setTargetTypeLocalName("DISTURBANCEEVIDENCE");
		result.setValuePropertyPath(Arrays.asList("disturbancetype", "commentary"));
		return result;
	}

	private Map<String, String> testProcess01_populate(Dataset ds) {
		Map<String, String> result = new HashMap<>();
		// Common graph
		String commonGraphName = TEST_PROJECT_NAMESPACE + "test_common#";
		Model commonModel = ds.getNamedModel(commonGraphName);
		// Project related graph
		String projectGraphName = TEST_PROJECT_NAMESPACE + "test_project#";
		Model model = ds.getNamedModel(projectGraphName);
		String studyLocationSubjectUri = TEST_PROJECT_NAMESPACE + "STUDYLOCATIONSUBGRAPH-T1493794712603";
		Resource studyLocationSubject = model.createResource(studyLocationSubjectUri);
		h.addBag(studyLocationSubject, "views", viewsBag -> {
			h.addBagElement(viewsBag, view -> {
				h.addBag(view, "observeditems", oiBag -> {
					h.addBagElement(oiBag, de -> {
						h.addType(de, "DISTURBANCEEVIDENCE");
						h.addResource(de, "disturbancetype", dt -> {
							h.addLiteral(dt, "commentary", "the value!");
						});
					});
				});
			});
		});
		result.put(COMMON_GRAPH, commonGraphName);
		result.put(ENV_RECORD_RDF_GRAPH, projectGraphName);
		result.put(ENV_RECORD_RDF_SUBJECT, studyLocationSubjectUri);
		return result;
	}
}
