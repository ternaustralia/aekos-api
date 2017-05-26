package au.org.aekos.api.producer.step;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;

/**
 * Decorates another extractor but allows us to make a inverse property path step before
 * we delegate to the other extractor.
 */
public class InversePropertyDecoratedAttributeExtractor implements AttributeExtractor {

	private String inversePropertyLocalName;
	private String targetResourceTypeLocalName;
	private AttributeExtractor delegate;
	private Dataset ds;
	private String propertyAndTypeNamespace;

	@Override
	public AttributeRecord doExtractOn(Resource subject) {
		Resource targetResource = followInverseProperty(subject);
		return delegate.doExtractOn(targetResource);
	}
	
	@Override
	public String getId() {
		return delegate.getId();
	}
	
	@Override
	public boolean canHandle(Resource subject) {
		// TODO how expensive is doing this SPARQL check twice? Once here and again when we do the extract.
		String sparql = String.format(
			"PREFIX aekos: <%s> " +
			"ASK " +
			// FIXME can we cut down the processing by supplying the graph
			"{ GRAPH ?g {" +
				getGraphPatternBlock(subject) +
			"}}", propertyAndTypeNamespace);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			return qexec.execAsk();
		}
	}
	
	private Resource followInverseProperty(Resource subject) {
		String sparql = String.format(
			"PREFIX aekos: <%s> " +
			"SELECT ?target " +
			// FIXME can we cut down the processing by supplying the graph
			"WHERE { GRAPH ?g { " +
				getGraphPatternBlock(subject) +
			"}}", propertyAndTypeNamespace);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet soln = qexec.execSelect();
			if (!soln.hasNext()) {
				String template = "Data problem: couldn't find '%s' inverse property from type '%s' pointing to '%s'";
				throw new IllegalStateException(String.format(template, inversePropertyLocalName, targetResourceTypeLocalName, subject.getURI()));
			}
			Resource result = soln.next().getResource("target");
			if (soln.hasNext()) {
				String template = "Data warning: found more than one link for the '%s' inverse property from type '%s' pointing to '%s'";
				throw new IllegalStateException(String.format(template, inversePropertyLocalName, targetResourceTypeLocalName, subject.getURI()));
			}
			Resource referenceInCorrectGraph = subject.getModel().getResource(result.getURI());
			return referenceInCorrectGraph;
		}
	}
	
	private String getGraphPatternBlock(Resource subject) {
		return String.format(
				"  <%s> ^aekos:%s ?target . " +
				"  ?target a aekos:%s . ",
				subject.getURI(), inversePropertyLocalName, targetResourceTypeLocalName);
	}

	public void setInversePropertyLocalName(String inversePropertyLocalName) {
		this.inversePropertyLocalName = inversePropertyLocalName;
	}

	public void setTargetResourceTypeLocalName(String targetResourceTypeLocalName) {
		this.targetResourceTypeLocalName = targetResourceTypeLocalName;
	}

	public void setDelegate(AttributeExtractor delegate) {
		this.delegate = delegate;
	}

	public void setDs(Dataset ds) {
		this.ds = ds;
	}

	public void setPropertyAndTypeNamespace(String propertyAndTypeNamespace) {
		this.propertyAndTypeNamespace = propertyAndTypeNamespace;
	}
}
