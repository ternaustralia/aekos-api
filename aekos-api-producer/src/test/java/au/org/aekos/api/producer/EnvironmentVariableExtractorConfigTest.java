package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.step.BagAttributeExtractor;
import au.org.aekos.api.producer.step.env.EnvItemProcessor;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.EnvVarRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;

public class EnvironmentVariableExtractorConfigTest {

	private static final String NS = "http://www.aekos.org.au/ontology/1.0.0";
	private static final String PROPERTY_NAMESPACE = NS + "#";
	private static final String COMMON_GRAPH_NAME = NS + "common#";
	private static final String PROJECT_GRAPH_NAME = NS + "project#";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Is the 'latestLandUse' extractor configured correctly?
	 */
	@Test
	public void testConfig01() throws Throwable {
		OutputEnvWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addBag(subject, "observeditems", oi -> {
				h.addBagElement(oi, e -> {
					h.addType(e, "SAMPLEDAREA");
					h.addResource(e, "latestlanduse", r -> {
						h.addResource(r, "category", r2 -> {
							h.addLiteral(r2, "name", "conservation");
						});
					});
				});
			});
		});
		List<EnvVarRecord> vars = result.getEnvVarRecords();
		assertThat(vars.size(), is(1));
		EnvVarRecord var = vars.get(0);
		assertThat(var.getName(), is("\"latestLandUse\""));
		assertThat(var.getValue(), is("\"conservation\""));
		assertThat(var.getUnits(), is(Utils.MYSQL_NULL));
	}
	
	/**
	 * Is the 'aspect' extractor configured correctly?
	 */
	@Test
	public void testConfig02() throws Throwable {
		OutputEnvWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource degreesUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(degreesUnit, "name", "degrees");
			h.addBag(subject, "observeditems", oi -> {
				h.addBagElement(oi, e -> {
					h.addType(e, "LANDSCAPE");
					h.addResource(e, "aspect", aspect -> {
						h.addLiteral(aspect, "value", "4");
						h.addResource(aspect, "units", degreesUnit);
					});
				});
			});
		});
		List<EnvVarRecord> vars = result.getEnvVarRecords();
		assertThat(vars.size(), is(1));
		EnvVarRecord var = vars.get(0);
		assertThat(var.getName(), is("\"aspect\""));
		assertThat(var.getValue(), is("\"4\""));
		assertThat(var.getUnits(), is("\"degrees\""));
	}
	
	private OutputEnvWrapper populateDataAndRunExtractionLoop(Consumer<Data> subjectCallback) throws Exception {
		Dataset ds = DatasetFactory.create();
		Model commonGraph = ModelFactory.createDefaultModel();
		ExtractionHelper extractionHelper = new ExtractionHelper(PROPERTY_NAMESPACE);
		extractionHelper.setCommonGraph(commonGraph);
		Model projectGraph = ModelFactory.createDefaultModel();
		Resource subject = projectGraph.createResource(PROJECT_GRAPH_NAME + "someVisit1");
		subjectCallback.accept(new Data(subject, commonGraph, projectGraph));
		ds.addNamedModel(COMMON_GRAPH_NAME, commonGraph);
		ds.addNamedModel(PROJECT_GRAPH_NAME, projectGraph);
		List<BagAttributeExtractor> objectUnderTest = EnvironmentVariableExtractorConfig.getExtractors(extractionHelper);
		EnvItemProcessor eip = new EnvItemProcessor();
		eip.setExtractors(objectUnderTest);
		eip.setHelper(extractionHelper);
		eip.setDataset(ds);
		InputEnvRecord item = new InputEnvRecord("not important", 1, 1, "not important", "not important", "not important",
				subject.getURI(), PROJECT_GRAPH_NAME, "not important", 1, 1);
		return eip.process(item);
	}
	
	private class Data {
		private final Resource subject;
		private final Model commonGraph;
		private final Model projectGraph;
		
		public Data(Resource subject, Model commonGraph, Model projectGraph) {
			this.subject = subject;
			this.commonGraph = commonGraph;
			this.projectGraph = projectGraph;
		}
	}
}
