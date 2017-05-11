package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.step.MissingDataException;

public class ExtractionHelperTest {

	private static final String PROPERTY_NAMESPACE = "urn:";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we generate a property when we supply a valid name?
	 */
	@Test
	public void testProp01() {
		ExtractionHelper objectUnderTest = new ExtractionHelper("urn:");
		Property result = objectUnderTest.prop("someProp");
		assertThat(result.getURI().toString(), is("urn:someProp"));
	}

	/**
	 * Can we get a literal when it is present and of the correct type?
	 */
	@Test
	public void testGetLiteral01() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource();
		h.addLiteral(subject, "someProp", "blah");
		String result = objectUnderTest.getLiteral(subject, "someProp");
		assertThat(result, is("blah"));
	}
	
	/**
	 * Can we report a useful error when we pass a null subject?
	 */
	@Test
	public void testGetLiteral02() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Resource nullSubject = null;
		try {
			objectUnderTest.getLiteral(nullSubject, "someProp");
			fail();
		} catch (IllegalStateException e) {
			// success
		}
	}
	
	/**
	 * Can we report a useful error when we request a property that doesn't exist?
	 */
	@Test
	public void testGetLiteral03() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource("urn:someRecord1");
		h.addLiteral(subject, "someProp", "blah");
		try {
			objectUnderTest.getLiteral(subject, "notThere");
			fail();
		} catch (MissingDataException e) {
			// success
		}
	}
	
	/**
	 * Can we get a literal when it is present but in another (named) graph?
	 */
	@Test
	public void testGetLiteral04() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Dataset ds = DatasetFactory.create();
		Model commonGraph = ds.getNamedModel("urn:common");
		Model projectGraph = ds.getNamedModel("urn:project");
		objectUnderTest.setCommonGraph(commonGraph);
		String subjectUri = "urn:sub1";
		Resource subjectInCommon = commonGraph.createResource(subjectUri);
		h.addLiteral(subjectInCommon, "someProp", "blah");
		Resource subjectInProject = projectGraph.createResource(subjectUri);
		String result = objectUnderTest.getLiteral(subjectInProject, "someProp");
		assertThat(result, is("blah"));
	}
	
	/**
	 * Can we coerce a non-String literal to a String?
	 */
	@Test
	public void testGetLiteral05() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource();
		h.addLiteral(subject, "someProp", 123);
		String result = objectUnderTest.getLiteral(subject, "someProp");
		assertThat(result, is("123"));
	}
	
	/**
	 * Can we get a resource when it is present and of the correct type?
	 */
	@Test
	public void testGetResource01() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource();
		h.addResource(subject, "someProp", r -> {
			h.addLiteral(r, "inTheNested", "yep");
		});
		Resource result = objectUnderTest.getResource(subject, "someProp");
		String proofWeHaveTheRightResource = objectUnderTest.getLiteral(result, "inTheNested");
		assertThat(proofWeHaveTheRightResource, is("yep"));
	}
	
	/**
	 * Can we get a resource when it is present but in another (named) graph?
	 */
	@Test
	public void testGetResource02() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Dataset ds = DatasetFactory.create();
		Model commonGraph = ds.getNamedModel("urn:common");
		Model projectGraph = ds.getNamedModel("urn:project");
		objectUnderTest.setCommonGraph(commonGraph);
		String subjectUri = "urn:sub1";
		Resource subjectInCommon = commonGraph.createResource(subjectUri);
		h.addResource(subjectInCommon, "someProp", r -> {
			h.addLiteral(r, "inTheNested", "yep");
		});
		Resource subjectInProject = projectGraph.createResource(subjectUri);
		Resource result = objectUnderTest.getResource(subjectInProject, "someProp");
		String proofWeHaveTheRightResource = objectUnderTest.getLiteral(result, "inTheNested");
		assertThat(proofWeHaveTheRightResource, is("yep"));
	}
	
	/**
	 * Can we report a useful error when the object is not a resource?
	 * 
	 */
	@Test
	public void testGetResource03() {
		ExtractionHelper objectUnderTest = new ExtractionHelper(PROPERTY_NAMESPACE);
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource("urn:someSub1");
		h.addLiteral(subject, "someProp", "not a resource");
		try {
			objectUnderTest.getResource(subject, "someProp");
			fail();
		} catch (IllegalStateException e) {
			// success
		}
	}
}
