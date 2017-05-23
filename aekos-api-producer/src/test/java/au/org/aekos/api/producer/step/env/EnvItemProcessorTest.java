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
import au.org.aekos.api.producer.step.BagAttributeExtractor;
import au.org.aekos.api.producer.step.PropertyPathNoUnitsBagAttributeExtractor;
import au.org.aekos.api.producer.step.PropertyPathWithUnitsBagAttributeExtractor;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.EnvVarRecord;
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
		objectUnderTest.setExtractors(Arrays.asList(disturbanceEvidenceExtractor()));
		objectUnderTest.setDataset(ds);
		objectUnderTest.setHelper(helper);
		InputEnvRecord item = new InputEnvRecord("aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002", 0, 0, "GDA94",
				"Null island", "not/important", dataFromRdf.get(ENV_RECORD_RDF_SUBJECT), dataFromRdf.get(ENV_RECORD_RDF_GRAPH), "2012-09-26", 9, 2012);
		OutputEnvWrapper result = objectUnderTest.process(item);
		List<EnvVarRecord> variables = result.getEnvVarRecords();
		assertThat(variables.size(), is(1));
		assertThat(variables.get(0).getLocationID(), is("\"aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002\""));
		assertThat(variables.get(0).getEventDate(), is("\"2012-09-26\""));
		assertThat(variables.get(0).getName(), is("\"disturbanceEvidence\""));
		assertThat(variables.get(0).getValue(), is("\"the value!\""));
	}
	
	/**
	 * Can we process the same resource with more than one extractor?
	 */
	@Test
	public void testProcess02() throws Throwable {
		EnvItemProcessor objectUnderTest = new EnvItemProcessor();
		Dataset ds = DatasetFactory.create();
		Map<String, String> dataFromRdf = testProcess02_populate(ds);
		helper.setCommonGraph(ds.getNamedModel(dataFromRdf.get(COMMON_GRAPH)));
		objectUnderTest.setExtractors(Arrays.asList(aspectExtractor(), slopeExtractor()));
		objectUnderTest.setDataset(ds);
		objectUnderTest.setHelper(helper);
		InputEnvRecord item = new InputEnvRecord("aekos.org.au/collection/adelaide.edu.au/TAF/TCFTNS0002", 0, 0, "GDA94",
				"Null island", "not/important", dataFromRdf.get(ENV_RECORD_RDF_SUBJECT), dataFromRdf.get(ENV_RECORD_RDF_GRAPH), "2012-09-26", 9, 2012);
		OutputEnvWrapper result = objectUnderTest.process(item);
		List<EnvVarRecord> variables = result.getEnvVarRecords();
		assertThat(variables.size(), is(2));
		EnvVarRecord first = variables.get(0);
		assertThat(first.getName(), is("\"aspect\""));
		assertThat(first.getValue(), is("\"250\""));
		assertThat(first.getUnits(), is("\"degrees\""));
		EnvVarRecord second = variables.get(1);
		assertThat(second.getName(), is("\"slope\""));
		assertThat(second.getValue(), is("\"4\""));
		assertThat(second.getUnits(), is("\"degrees\""));
	}

	private BagAttributeExtractor disturbanceEvidenceExtractor() {
		PropertyPathNoUnitsBagAttributeExtractor result = new PropertyPathNoUnitsBagAttributeExtractor();
		result.setHelper(helper);
		result.setFinalName("disturbanceEvidence");
		result.setTargetTypeLocalName("DISTURBANCEEVIDENCE");
		result.setValuePropertyPath(Arrays.asList("disturbancetype", "commentary"));
		return result;
	}
	
	private BagAttributeExtractor aspectExtractor() {
		PropertyPathWithUnitsBagAttributeExtractor result = new PropertyPathWithUnitsBagAttributeExtractor();
		result.setHelper(helper);
		result.setFinalName("aspect");
		result.setTargetTypeLocalName("LANDSCAPE");
		result.setValuePropertyPath(Arrays.asList("aspect", "value"));
		result.setUnitsPropertyPath(Arrays.asList("aspect", "units", "name"));
		return result;
	}
	
	private BagAttributeExtractor slopeExtractor() {
		PropertyPathWithUnitsBagAttributeExtractor result = new PropertyPathWithUnitsBagAttributeExtractor();
		result.setHelper(helper);
		result.setFinalName("slope");
		result.setTargetTypeLocalName("LANDSCAPE");
		result.setValuePropertyPath(Arrays.asList("slope", "value"));
		result.setUnitsPropertyPath(Arrays.asList("slope", "units", "name"));
		return result;
	}

	private Map<String, String> testProcess01_populate(Dataset ds) {
		Map<String, String> result = new HashMap<>();
		// Project related graph
		String projectGraphName = TEST_PROJECT_NAMESPACE + "test_project#";
		Model model = ds.getNamedModel(projectGraphName);
		Resource studyLocationSubject = model.createResource(TEST_PROJECT_NAMESPACE + "STUDYLOCATIONSUBGRAPH-T1493794712603");
		h.addBag(studyLocationSubject, "views", viewsBag -> {
			Resource view = model.createResource(TEST_PROJECT_NAMESPACE + "STUDYLOCATIONVISITVIEW-T1493794229804");
			h.addBag(view, "observeditems", oiBag -> {
				h.addBagElement(oiBag, de -> {
					h.addType(de, "DISTURBANCEEVIDENCE");
					h.addResource(de, "disturbancetype", dt -> {
						h.addLiteral(dt, "commentary", "the value!");
					});
				});
			});
			h.addBagElement(viewsBag, view);
			result.put(ENV_RECORD_RDF_SUBJECT, view.getURI());
		});
		result.put(ENV_RECORD_RDF_GRAPH, projectGraphName);
		return result;
	}
	
	private Map<String, String> testProcess02_populate(Dataset ds) {
		Map<String, String> result = new HashMap<>();
		// Common graph
		String commonGraphName = TEST_PROJECT_NAMESPACE + "test_common#";
		Model commonModel = ds.getNamedModel(commonGraphName);
		Resource measurementUnit = commonModel.createResource("urn:degreesUnits");
		{
			h.addLiteral(measurementUnit, "name", "degrees");
		}
		// Project related graph
		String projectGraphName = TEST_PROJECT_NAMESPACE + "test_project#";
		Model model = ds.getNamedModel(projectGraphName);
		Resource studyLocationSubject = model.createResource(TEST_PROJECT_NAMESPACE + "STUDYLOCATIONSUBGRAPH-T1493794712603");
		{
			h.addBag(studyLocationSubject, "views", viewsBag -> {
				Resource view = model.createResource("");
				h.addBag(view, "observeditems", oiBag -> {
					h.addBagElement(oiBag, ls -> {
						h.addType(ls, "LANDSCAPE");
						h.addResource(ls, "aspect", aspect -> {
							h.addLiteral(aspect, "value", "250");
							h.addResource(aspect, "units", measurementUnit);
						});
						h.addResource(ls, "slope", slope -> {
							h.addLiteral(slope, "value", "4");
							h.addResource(slope, "units", measurementUnit);
						});
					});
				});
				h.addBagElement(viewsBag, view);
				result.put(ENV_RECORD_RDF_SUBJECT, view.getURI());
			});
		}
		result.put(COMMON_GRAPH, commonGraphName);
		result.put(ENV_RECORD_RDF_GRAPH, projectGraphName);
		return result;
	}
}
