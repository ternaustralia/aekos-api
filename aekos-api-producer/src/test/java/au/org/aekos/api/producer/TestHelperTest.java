package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;

public class TestHelperTest {

	/**
	 * Can we add a bag of resources (and see they don't come out in order)?
	 */
	@Test
	public void testAddBag01() {
		TestHelper objectUnderTest = new TestHelper("urn:");
		Model m = ModelFactory.createDefaultModel();
		Resource subject = m.createResource("urn:theSub1");
		objectUnderTest.addBag(subject, "theBag", bag -> {
			objectUnderTest.addBagElement(bag, e -> {
				objectUnderTest.addLiteral(e, "position", "first");
			});
			objectUnderTest.addBagElement(bag, e -> {
				objectUnderTest.addLiteral(e, "position", "second");
			});
		});
		Bag result = subject.getProperty(m.createProperty("urn:theBag")).getBag();
		assertThat(result.size(), is(2));
		StmtIterator it = result.listProperties();
		assertThat(it.next().getResource().getProperty(m.createProperty("urn:position")).getString(), is("second"));
		assertThat(it.next().getResource().getProperty(m.createProperty("urn:position")).getString(), is("first"));
	}
	
	/**
	 * Can we create a bag element?
	 */
	@Test
	public void testAddBagElement01() {
		TestHelper objectUnderTest = new TestHelper("urn:");
		Bag bag = ModelFactory.createDefaultModel().createBag();
		objectUnderTest.addBagElement(bag, e -> {
			objectUnderTest.addLiteral(e, "someProp", "inFirstElement");
		});
		Resource result = bag.iterator().next().asResource();
		String literalValue = result.getProperty(result.getModel().createProperty("urn:someProp")).getLiteral().getString();
		assertThat(literalValue, is("inFirstElement"));
	}
}
