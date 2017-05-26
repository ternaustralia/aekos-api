package au.org.aekos.api.producer;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.aekos.api.producer.step.MissingDataException;

public class ExtractionHelper {

	private static final Logger logger = LoggerFactory.getLogger(ExtractionHelper.class);
	private final Model helperModel = ModelFactory.createDefaultModel();
	private final String propertyNamespace;
	private Model commonGraph;

	public ExtractionHelper(String propertyNamespace) {
		this.propertyNamespace = propertyNamespace;
	}

	public Property prop(String propertyName) {
		return helperModel.createProperty(propertyNamespace + propertyName);
	}

	public String getLiteral(Resource subject, String propertyName) {
		Property p = prop(propertyName);
		RDFNode object = validateAndGetObject(subject, p);
		if (!object.isLiteral()) {
			String template = "Data problem: expected value of '%s' on '%s' to be a literal";
			throw new IllegalStateException(String.format(template, p.getURI(), subject.getURI()));
		}
		return object.asLiteral().getString();
	}

	public Resource getResource(Resource subject, String propertyName) {
		Property p = prop(propertyName);
		RDFNode object = validateAndGetObject(subject, p);
		if (!object.isResource()) {
			String template = "Data problem: expected value of '%s' on '%s' to be a resource";
			throw new IllegalStateException(String.format(template, p.getURI(), subject.getURI()));
		}
		return object.asResource();
	}
	
	public Bag getBag(Resource subject, String propertyName) {
		Resource possibleBag = getResource(subject, propertyName);
		if (!possibleBag.canAs(Bag.class)) {
			throw new IllegalStateException("Data problem: expected " + propertyName + " to be a bag");
		}
		return possibleBag.as(Bag.class);
	}
	
	public boolean hasProperty(Resource subject, String localPropertyName) {
		return subject.hasProperty(prop(localPropertyName));
	}

	private RDFNode validateAndGetObject(Resource subject, Property p) {
		if (subject == null) {
			throw new IllegalStateException("Data problem: supplied subject is null.");
		}
		Model projectGraph = subject.getModel();
		Model model;
		if (commonGraph == null) {
			logger.warn("ExtractionHelper was not provided a 'common' graph to search, only the project graph will be used");
			model = projectGraph;
		} else {
			model = ModelFactory.createUnion(projectGraph, commonGraph);
		}
		Statement statement = model.getProperty(subject, p);
		if (statement == null) {
			String template = "Data problem: couldn't find '%s' property on '%s'";
			String uri = subject.getURI();
			if (uri == null) {
				uri = "(anonymous subject)";
			}
			throw new MissingDataException(String.format(template, p.getURI(), uri));
		}
		RDFNode object = statement.getObject();
		return object;
	}

	public void setCommonGraph(Model commonGraph) {
		this.commonGraph = commonGraph;
	}
}
