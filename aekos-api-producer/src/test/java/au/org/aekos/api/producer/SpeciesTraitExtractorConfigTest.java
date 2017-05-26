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

import au.org.aekos.api.producer.step.AttributeExtractor;
import au.org.aekos.api.producer.step.species.SpeciesItemProcessor;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.SpeciesTraitRecord;

public class SpeciesTraitExtractorConfigTest {

	private static final String MORPHOMETRICS_LOCAL_TYPE_NAME = "MORPHOMETRICS";
	private static final String NS = "http://www.aekos.org.au/ontology/1.0.0";
	private static final String PROPERTY_NAMESPACE = NS + "#";
	private static final String COMMON_GRAPH_NAME = NS + "common#";
	private static final String PROJECT_GRAPH_NAME = NS + "project#";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Is the 'averageHeight' extractor configured correctly?
	 */
	@Test
	public void testConfig01() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource metresUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(metresUnit, "name", "metres");
			h.addResource(subject, "averageheight", r -> {
				h.addLiteral(r, "value", "4");
				h.addResource(r, "units", metresUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"averageHeight\""));
		assertThat(trait.getValue(), is("\"4\""));
		assertThat(trait.getUnits(), is("\"metres\""));
	}
	
	/**
	 * Is the 'basalArea' extractor configured correctly?
	 */
	@Test
	public void testConfig02() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addResource(subject, "basalarea", r -> {
				h.addLiteral(r, "value", "0.1");
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"basalArea\""));
		assertThat(trait.getValue(), is("\"0.1\""));
		assertThat(trait.getUnits(), is("\"square metres per hectare\""));
	}

	/**
	 * Is the 'basalAreaFactor' extractor configured correctly?
	 */
	@Test
	public void testConfig03() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			h.addResource(data.subject, "basalareafactor", r -> {
				h.addLiteral(r, "value", "4");
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"basalAreaFactor\""));
		assertThat(trait.getValue(), is("\"4\""));
		assertThat(trait.getUnits(), is(Utils.MYSQL_NULL));
	}
	
	/**
	 * Is the 'basalAreaCount' extractor configured correctly?
	 */
	@Test
	public void testConfig04() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			h.addResource(data.subject, "basalareacount", r -> {
				h.addLiteral(r, "value", "4");
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"basalAreaCount\""));
		assertThat(trait.getValue(), is("\"4\""));
		assertThat(trait.getUnits(), is(Utils.MYSQL_NULL));
	}
	
	/**
	 * Is the 'biomass' extractor configured correctly?
	 */
	@Test
	public void testConfig05() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource percentUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(percentUnit, "name", "percent");
			h.addResource(subject, "biomass", r -> {
				h.addLiteral(r, "value", "90");
				h.addResource(r, "units", percentUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"biomass\""));
		assertThat(trait.getValue(), is("\"90\""));
		assertThat(trait.getUnits(), is("\"percent\""));
	}
	
	/**
	 * Is the 'canopyCover' extractor configured correctly?
	 */
	@Test
	public void testConfig06() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource percentUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(percentUnit, "name", "percent");
			h.addResource(subject, "canopycover", r -> {
				h.addLiteral(r, "value", "90");
				h.addResource(r, "units", percentUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"canopyCover\""));
		assertThat(trait.getValue(), is("\"90\""));
		assertThat(trait.getUnits(), is("\"percent\""));
	}
	
	/**
	 * Is the 'cover' extractor configured correctly?
	 */
	@Test
	public void testConfig07() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource percentUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(percentUnit, "name", "percent");
			h.addResource(subject, "cover", r -> {
				h.addLiteral(r, "value", "90");
				h.addResource(r, "units", percentUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"cover\""));
		assertThat(trait.getValue(), is("\"90\""));
		assertThat(trait.getUnits(), is("\"percent\""));
	}
	
	/**
	 * Is the 'height' extractor configured correctly?
	 */
	@Test
	public void testConfig08() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			Resource metresUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(metresUnit, "name", "metres");
			h.addResource(subject, "height", r -> {
				h.addLiteral(r, "value", "4");
				h.addResource(r, "units", metresUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"height\""));
		assertThat(trait.getValue(), is("\"4\""));
		assertThat(trait.getUnits(), is("\"metres\""));
	}
	
	/**
	 * Is the 'billLength' extractor configured correctly?
	 */
	@Test
	public void testConfig09() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addType(subject, "INDIVIDUALORGANISM");
			Resource morphometrics = data.projectGraph.createResource(PROJECT_GRAPH_NAME + MORPHOMETRICS_LOCAL_TYPE_NAME + "-T1493794184418");
			h.addType(morphometrics, MORPHOMETRICS_LOCAL_TYPE_NAME);
			Resource mmUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(mmUnit, "name", "millimetres");
			h.addResource(morphometrics, "featureof", subject);
			h.addResource(morphometrics, "billlength", r -> {
				h.addLiteral(r, "value", "82");
				h.addResource(r, "units", mmUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"billLength\""));
		assertThat(trait.getValue(), is("\"82\""));
		assertThat(trait.getUnits(), is("\"millimetres\""));
	}
	
	/**
	 * Is the 'billLengthShield' extractor configured correctly?
	 */
	@Test
	public void testConfig10() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addType(subject, "INDIVIDUALORGANISM");
			Resource morphometrics = data.projectGraph.createResource(PROJECT_GRAPH_NAME + MORPHOMETRICS_LOCAL_TYPE_NAME + "-T1493794184418");
			h.addType(morphometrics, MORPHOMETRICS_LOCAL_TYPE_NAME);
			Resource mmUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(mmUnit, "name", "millimetres");
			h.addResource(morphometrics, "featureof", subject);
			h.addResource(morphometrics, "billlengthshield", r -> {
				h.addLiteral(r, "value", "11");
				h.addResource(r, "units", mmUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"billLengthShield\""));
		assertThat(trait.getValue(), is("\"11\""));
		assertThat(trait.getUnits(), is("\"millimetres\""));
	}
	
	/**
	 * Is the 'billWidth' extractor configured correctly?
	 */
	@Test
	public void testConfig11() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addType(subject, "INDIVIDUALORGANISM");
			Resource morphometrics = data.projectGraph.createResource(PROJECT_GRAPH_NAME + MORPHOMETRICS_LOCAL_TYPE_NAME + "-T1493794184418");
			h.addType(morphometrics, MORPHOMETRICS_LOCAL_TYPE_NAME);
			Resource mmUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(mmUnit, "name", "millimetres");
			h.addResource(morphometrics, "featureof", subject);
			h.addResource(morphometrics, "billwidth", r -> {
				h.addLiteral(r, "value", "55");
				h.addResource(r, "units", mmUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"billWidth\""));
		assertThat(trait.getValue(), is("\"55\""));
		assertThat(trait.getUnits(), is("\"millimetres\""));
	}
	
	/**
	 * Is the 'totalLength' extractor configured correctly?
	 */
	@Test
	public void testConfig12() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addType(subject, "INDIVIDUALORGANISM");
			Resource morphometrics = data.projectGraph.createResource(PROJECT_GRAPH_NAME + MORPHOMETRICS_LOCAL_TYPE_NAME + "-T1493794184418");
			h.addType(morphometrics, MORPHOMETRICS_LOCAL_TYPE_NAME);
			Resource mmUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(mmUnit, "name", "millimetres");
			h.addResource(morphometrics, "featureof", subject);
			h.addResource(morphometrics, "totallength", r -> {
				h.addLiteral(r, "value", "430");
				h.addResource(r, "units", mmUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"totalLength\""));
		assertThat(trait.getValue(), is("\"430\""));
		assertThat(trait.getUnits(), is("\"millimetres\""));
	}
	
	/**
	 * Is the 'weight' extractor configured correctly?
	 */
	@Test
	public void testConfig13() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			Resource subject = data.subject;
			h.addType(subject, "INDIVIDUALORGANISM");
			Resource morphometrics = data.projectGraph.createResource(PROJECT_GRAPH_NAME + MORPHOMETRICS_LOCAL_TYPE_NAME + "-T1493794184418");
			h.addType(morphometrics, MORPHOMETRICS_LOCAL_TYPE_NAME);
			Resource mmUnit = data.commonGraph.createResource(COMMON_GRAPH_NAME + "theUnit");
			h.addLiteral(mmUnit, "name", "grams");
			h.addResource(morphometrics, "featureof", subject);
			h.addResource(morphometrics, "weight", r -> {
				h.addLiteral(r, "value", "1930");
				h.addResource(r, "units", mmUnit);
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord trait = traits.get(0);
		assertThat(trait.getName(), is("\"weight\""));
		assertThat(trait.getValue(), is("\"1930\""));
		assertThat(trait.getUnits(), is("\"grams\""));
	}

	private OutputSpeciesWrapper populateDataAndRunExtractionLoop(Consumer<Data> subjectCallback) throws Exception {
		Dataset ds = DatasetFactory.create();
		Model commonGraph = ModelFactory.createDefaultModel();
		ExtractionHelper extractionHelper = new ExtractionHelper(PROPERTY_NAMESPACE);
		extractionHelper.setCommonGraph(commonGraph);
		Model projectGraph = ModelFactory.createDefaultModel();
		Resource subject = projectGraph.createResource(PROJECT_GRAPH_NAME + "someSubject1");
		subjectCallback.accept(new Data(subject, commonGraph, projectGraph));
		ds.addNamedModel(COMMON_GRAPH_NAME, commonGraph);
		ds.addNamedModel(PROJECT_GRAPH_NAME, projectGraph);
		List<AttributeExtractor> objectUnderTest = SpeciesTraitExtractorConfig.getExtractors(extractionHelper, PROPERTY_NAMESPACE,
				ds, MORPHOMETRICS_LOCAL_TYPE_NAME);
		SpeciesItemProcessor sip = new SpeciesItemProcessor();
		sip.setExtractors(objectUnderTest);
		sip.setDataset(ds);
		InputSpeciesRecord item = new InputSpeciesRecord("some-id-1234", subject.getURI(), PROJECT_GRAPH_NAME,
				1, "not important", "not important", "not important", "not important");
		OutputSpeciesWrapper result = sip.process(item);
		return result;
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
