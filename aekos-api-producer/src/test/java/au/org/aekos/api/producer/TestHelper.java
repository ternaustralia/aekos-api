package au.org.aekos.api.producer;

import java.util.function.Consumer;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class TestHelper {

	private final Model helperModel = ModelFactory.createDefaultModel();
	private final String propertyNamespace;
	
	public TestHelper(String propertyNamespace) {
		this.propertyNamespace = propertyNamespace;
	}

	public void addLiteral(Resource subject, String propertyName, String value) {
		subject.addLiteral(prop(propertyName), value);
	}
	
	public void addLiteral(Resource subject, String propertyName, int value) {
		subject.addLiteral(prop(propertyName), value);
	}
	
	public void addResource(Resource subject, String propertyName, Consumer<Resource> callback) {
		Model model = subject.getModel();
		Resource object = model.createResource();
		subject.addProperty(prop(propertyName), object);
		callback.accept(object);
	}
	
	public void addResourceReference(Resource subject, String propertyName, Resource object) {
		subject.addProperty(prop(propertyName), object);
	}
	
	public void addBagElement(Bag bag, Consumer<Resource> callback) {
		Model model = bag.getModel();
		Resource element = model.createResource();
		callback.accept(element);
		bag.add(element);
	}
	
	public void addBag(Resource subject, String propertyName, Consumer<Bag> elementCallback) {
		Model model = subject.getModel();
		Bag bag = model.createBag();
		subject.addProperty(prop(propertyName), bag);
		elementCallback.accept(bag);
	}
	
	public void addType(Resource subject, String localTypeName) {
		Resource type = helperModel.createResource(propertyNamespace + localTypeName);
		subject.addProperty(RDF.type, type);
	}
	
	private Property prop(String propertyName) {
		return helperModel.createProperty(propertyNamespace + propertyName);
	}
}
