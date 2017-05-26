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

	private static final String NS = "http://www.aekos.org.au/ontology/1.0.0";
	private static final String PROPERTY_NAMESPACE = NS + "#";
	private static final String COMMON_GRAPH_NAME = NS + "common#";
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
			h.addResource(subject, "averageHeight", r -> {
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
	
	// TODO test basalArea

	/**
	 * Is the 'basalAreaFactor' extractor configured correctly?
	 */
	@Test
	public void testConfig02() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(data -> {
			h.addResource(data.subject, "basalAreaFactor", r -> {
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
	 * Is the 'biomass' extractor configured correctly?
	 */
	@Test
	public void testConfig03() throws Throwable {
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
	 * Is the 'height' extractor configured correctly?
	 */
	@Test
	public void testConfig04() throws Throwable {
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

	private OutputSpeciesWrapper populateDataAndRunExtractionLoop(Consumer<Data> subjectCallback) throws Exception {
		Dataset ds = DatasetFactory.create();
		Model commonGraph = ModelFactory.createDefaultModel();
		ds.addNamedModel(COMMON_GRAPH_NAME, commonGraph);
		ExtractionHelper extractionHelper = new ExtractionHelper(PROPERTY_NAMESPACE);
		extractionHelper.setCommonGraph(commonGraph);
		List<AttributeExtractor> objectUnderTest = SpeciesTraitExtractorConfig.getExtractors(extractionHelper);
		SpeciesItemProcessor sip = new SpeciesItemProcessor();
		sip.setExtractors(objectUnderTest);
		sip.setDataset(ds);
		String projectGraphName = NS + "project#";
		Model projectGraph = ModelFactory.createDefaultModel();
		Resource subject = projectGraph.createResource(projectGraphName + "someSubject1");
		subjectCallback.accept(new Data(subject, commonGraph));
		ds.addNamedModel(projectGraphName, projectGraph);
		InputSpeciesRecord item = new InputSpeciesRecord("some-id-1234", subject.getURI(), projectGraphName,
				1, "not important", "not important", "not important", "not important");
		OutputSpeciesWrapper result = sip.process(item);
		return result;
	}
	
	private class Data {
		private final Resource subject;
		private final Model commonGraph;
		public Data(Resource subject, Model commonGraph) {
			this.subject = subject;
			this.commonGraph = commonGraph;
		}
	}
}
