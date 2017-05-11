package au.org.aekos.api.producer;

import java.util.function.Consumer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

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

	private Property prop(String propertyName) {
		return helperModel.createProperty(propertyNamespace + propertyName);
	}
}
