package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Is the 'averageHeight' extractor configured correctly?
	 */
	@Test
	public void testConfig01() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(subject -> {
			h.addResource(subject, "averageHeight", r -> {
				h.addLiteral(r, "value", "4");
				h.addResource(r, "units", r2 -> {
					h.addLiteral(r2, "name", "metres");
				});
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord firstTrait = traits.get(0);
		assertThat(firstTrait.getName(), is("\"averageHeight\""));
		assertThat(firstTrait.getValue(), is("\"4\""));
		assertThat(firstTrait.getUnits(), is("\"metres\""));
	}
	
	/**
	 * Is the 'biomass' extractor configured correctly?
	 */
	@Test
	public void testConfig02() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(subject -> {
			h.addResource(subject, "biomass", r -> {
				h.addLiteral(r, "value", "90");
				h.addResource(r, "units", r2 -> {
					h.addLiteral(r2, "name", "percent");
				});
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord firstTrait = traits.get(0);
		assertThat(firstTrait.getName(), is("\"biomass\""));
		assertThat(firstTrait.getValue(), is("\"90\""));
		assertThat(firstTrait.getUnits(), is("\"percent\""));
	}
	
	/**
	 * Is the 'height' extractor configured correctly?
	 */
	@Test
	public void testConfig03() throws Throwable {
		OutputSpeciesWrapper result = populateDataAndRunExtractionLoop(subject -> {
			h.addResource(subject, "height", r -> {
				h.addLiteral(r, "value", "4");
				h.addResource(r, "units", r2 -> {
					h.addLiteral(r2, "name", "metres");
				});
			});
		});
		List<SpeciesTraitRecord> traits = result.getTraitRecords();
		assertThat(traits.size(), is(1));
		SpeciesTraitRecord firstTrait = traits.get(0);
		assertThat(firstTrait.getName(), is("\"height\""));
		assertThat(firstTrait.getValue(), is("\"4\""));
		assertThat(firstTrait.getUnits(), is("\"metres\""));
	}

	private OutputSpeciesWrapper populateDataAndRunExtractionLoop(Consumer<Resource> subjectCallback) throws Exception {
		Dataset ds = DatasetFactory.create();
		String commonGraphName = NS + "common#";
		Model commonGraph = ModelFactory.createDefaultModel();
		ds.addNamedModel(commonGraphName, commonGraph);
		ExtractionHelper extractionHelper = new ExtractionHelper(PROPERTY_NAMESPACE);
		extractionHelper.setCommonGraph(commonGraph);
		List<AttributeExtractor> objectUnderTest = SpeciesTraitExtractorConfig.getExtractors(extractionHelper);
		SpeciesItemProcessor sip = new SpeciesItemProcessor();
		sip.setExtractors(objectUnderTest);
		sip.setDataset(ds);
		String projectGraphName = NS + "project#";
		Model projectGraph = ModelFactory.createDefaultModel();
		Resource subject = projectGraph.createResource(projectGraphName + "someSubject1");
		subjectCallback.accept(subject);
		ds.addNamedModel(projectGraphName, projectGraph);
		InputSpeciesRecord item = new InputSpeciesRecord("some-id-1234", subject.getURI(), projectGraphName,
				1, "not important", "not important", "not important", "not important");
		OutputSpeciesWrapper result = sip.process(item);
		return result;
	}
}
