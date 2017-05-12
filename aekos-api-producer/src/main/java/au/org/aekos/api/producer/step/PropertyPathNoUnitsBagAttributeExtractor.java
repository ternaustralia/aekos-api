package au.org.aekos.api.producer.step;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.aekos.api.producer.ExtractionHelper;

public class PropertyPathNoUnitsBagAttributeExtractor implements BagAttributeExtractor {

	private static final Logger logger = LoggerFactory.getLogger(PropertyPathNoUnitsBagAttributeExtractor.class);
	private static final String NO_UNITS = "";
	private String targetTypeLocalName;
	private ExtractionHelper helper;
	private String finalName;
	private List<String> valuePropertyPath;

	@Override
	public AttributeRecord doExtractOn(Resource subject, String parentId) {
		Resource currPointInPath = subject;
		String value = null;
		try {
			for (Iterator<String> it = valuePropertyPath.iterator(); it.hasNext();) {
				String nextProperty = it.next();
				boolean isLast = !it.hasNext();
				if (isLast) {
					// treat as literal
					value = helper.getLiteral(currPointInPath, nextProperty);
				} else {
					// treat as resource
					currPointInPath = helper.getResource(currPointInPath, nextProperty);
				}
			}
			return new AttributeRecord(parentId, finalName, value, NO_UNITS);
		} catch (MissingDataException e) {
			String template = "Data problem: failed to find value for '%s' trait with path '%s' on '%s'";
			throw new MissingDataException(String.format(template, finalName, valuePropertyPath.toString(), subject.getURI()), e);
		}
	}

	@Override
	public String getId() {
		return finalName; // TODO do we need a separate ID when we have multiple targeting the same type?
	}

	@Override
	public boolean canHandle(Resource subject) {
		Statement statement = subject.getProperty(RDF.type);
		if (statement == null) {
			String template = "Data warning: supplied resource '%s' has NO TYPE, nothing can handle it!";
			logger.warn(String.format(template, subject.getURI()));
			return false;
		}
		Resource type = statement.getResource();
		return type.getLocalName().equals(targetTypeLocalName);
	}

	public void setTargetTypeLocalName(String targetTypeLocalName) {
		this.targetTypeLocalName = targetTypeLocalName;
	}

	public void setFinalName(String finalName) {
		this.finalName = finalName;
	}

	public void setValuePropertyPath(List<String> valuePropertyPath) {
		this.valuePropertyPath = valuePropertyPath;
	}

	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}
}
