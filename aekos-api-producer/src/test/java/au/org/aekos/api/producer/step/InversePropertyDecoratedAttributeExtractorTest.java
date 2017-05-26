package au.org.aekos.api.producer.step;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import au.org.aekos.api.producer.TestHelper;

public class InversePropertyDecoratedAttributeExtractorTest {

	private static final String NS = "http://www.aekos.org.au/ontology/1.0.0";
	private static final String PROPERTY_NAMESPACE = NS + "#";
	private static final String PROJECT_GRAPH_NAME = NS + "project#";
	private final TestHelper h = new TestHelper(PROPERTY_NAMESPACE);
	
	/**
	 * Can we find the target and pass it to the delegate when it exists?
	 */
	@Test
	public void testDoExtractOn01() {
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource(PROJECT_GRAPH_NAME + "someSub1");
		h.addType(subject, "INDIVIDUALORGANISM");
		Resource morphometrics = m.createResource(PROJECT_GRAPH_NAME + "MORPHOMETRICS-T1493794184418");
		h.addResource(morphometrics, "featureof", subject);
		h.addType(morphometrics, "MORPHOMETRICS");
		InversePropertyDecoratedAttributeExtractor objectUnderTest = new InversePropertyDecoratedAttributeExtractor();
		AttributeExtractor delegate = mock(AttributeExtractor.class);
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(PROJECT_GRAPH_NAME, m);
		
		objectUnderTest.setDelegate(delegate);
		objectUnderTest.setDs(ds);
		objectUnderTest.setPropertyAndTypeNamespace(PROPERTY_NAMESPACE);
		objectUnderTest.setInversePropertyLocalName("featureof");
		objectUnderTest.setTargetResourceTypeLocalName("MORPHOMETRICS");
		objectUnderTest.doExtractOn(subject);
		verify(delegate).doExtractOn(morphometrics);
	}

	/**
	 * Is the expected exception thrown when we don't find a solution?
	 */
	@Test
	public void testDoExtractOn02() {
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource(PROJECT_GRAPH_NAME + "someSub1");
		h.addType(subject, "INDIVIDUALORGANISM");
		InversePropertyDecoratedAttributeExtractor objectUnderTest = new InversePropertyDecoratedAttributeExtractor();
		AttributeExtractor delegate = mock(AttributeExtractor.class);
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(PROJECT_GRAPH_NAME, m);
		
		objectUnderTest.setDelegate(delegate);
		objectUnderTest.setDs(ds);
		objectUnderTest.setPropertyAndTypeNamespace(PROPERTY_NAMESPACE);
		objectUnderTest.setInversePropertyLocalName("featureof");
		objectUnderTest.setTargetResourceTypeLocalName("MORPHOMETRICS");
		try {
			objectUnderTest.doExtractOn(subject);
			fail();
		} catch (IllegalStateException e) {
			// success!
		}
	}
	
	/**
	 * Is the expected exception thrown when we find more than one link?
	 * Not sure if this will ever happen in the data but if it does, we're not prepared for it.
	 */
	@Test
	public void testDoExtractOn03() {
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource(PROJECT_GRAPH_NAME + "someSub1");
		h.addType(subject, "INDIVIDUALORGANISM");
		Resource morphometrics1 = m.createResource(PROJECT_GRAPH_NAME + "MORPHOMETRICS-T1111");
		h.addResource(morphometrics1, "featureof", subject);
		h.addType(morphometrics1, "MORPHOMETRICS");
		Resource morphometrics2 = m.createResource(PROJECT_GRAPH_NAME + "MORPHOMETRICS-T2222");
		h.addResource(morphometrics2, "featureof", subject);
		h.addType(morphometrics2, "MORPHOMETRICS");
		InversePropertyDecoratedAttributeExtractor objectUnderTest = new InversePropertyDecoratedAttributeExtractor();
		AttributeExtractor delegate = mock(AttributeExtractor.class);
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(PROJECT_GRAPH_NAME, m);
		
		objectUnderTest.setDelegate(delegate);
		objectUnderTest.setDs(ds);
		objectUnderTest.setPropertyAndTypeNamespace(PROPERTY_NAMESPACE);
		objectUnderTest.setInversePropertyLocalName("featureof");
		objectUnderTest.setTargetResourceTypeLocalName("MORPHOMETRICS");
		try {
			objectUnderTest.doExtractOn(subject);
			fail("Expected a thrown exception");
		} catch (IllegalStateException e) {
			// success!
		}
	}
	
	/**
	 * Can we tell when we can handle the extraction as the link exists (we don't check deeper than that though)?
	 */
	@Test
	public void testCanHandle01() {
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource(PROJECT_GRAPH_NAME + "someSub1");
		h.addType(subject, "INDIVIDUALORGANISM");
		Resource morphometrics = m.createResource(PROJECT_GRAPH_NAME + "MORPHOMETRICS-T1493794184418");
		h.addResource(morphometrics, "featureof", subject);
		h.addType(morphometrics, "MORPHOMETRICS");
		InversePropertyDecoratedAttributeExtractor objectUnderTest = new InversePropertyDecoratedAttributeExtractor();
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(PROJECT_GRAPH_NAME, m);
		
		objectUnderTest.setDs(ds);
		objectUnderTest.setPropertyAndTypeNamespace(PROPERTY_NAMESPACE);
		objectUnderTest.setInversePropertyLocalName("featureof");
		objectUnderTest.setTargetResourceTypeLocalName("MORPHOMETRICS");
		boolean result = objectUnderTest.canHandle(subject);
		assertTrue("Should be able to handle as the link exists", result);
	}
	
	/**
	 * Can we tell when we can NOT handle the extraction as the link DOES NOT exist?
	 */
	@Test
	public void testCanHandle02() {
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource(PROJECT_GRAPH_NAME + "someSub1");
		h.addType(subject, "INDIVIDUALORGANISM");
		InversePropertyDecoratedAttributeExtractor objectUnderTest = new InversePropertyDecoratedAttributeExtractor();
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(PROJECT_GRAPH_NAME, m);
		
		objectUnderTest.setDs(ds);
		objectUnderTest.setPropertyAndTypeNamespace(PROPERTY_NAMESPACE);
		objectUnderTest.setInversePropertyLocalName("featureof");
		objectUnderTest.setTargetResourceTypeLocalName("MORPHOMETRICS");
		boolean result = objectUnderTest.canHandle(subject);
		assertFalse("Should NOT be able to handle as the link DOES NOT exist", result);
	}
}
