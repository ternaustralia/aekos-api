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
	private static final String NO_UNITS = null;
	private String targetTypeLocalName;
	private ExtractionHelper helper;
	private String finalName;
	private List<String> valuePropertyPath;

	@Override
	public AttributeRecord doExtractOn(Resource subject) {
		try {
			String value = followPath(subject, valuePropertyPath);
			String units = getUnits(subject);
			return new AttributeRecord(finalName, value, units);
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
	
	protected String getUnits(Resource startingResource) {
		return NO_UNITS;
	}
	
	protected String followPath(Resource startingResource, List<String> propertyPath) {
		Resource currPointInPath = startingResource;
		String value = null;
		for (Iterator<String> it = propertyPath.iterator(); it.hasNext();) {
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
		return value;
	}

	/**
	 * Local name of the type this extractor is looking for. For example,
	 * if you have an entity of type <code>aekos:SPECIESORGANISMGROUPFAUNA</code>
	 * then the <code>targetTypeLocalName</code> is <code>"SPECIESORGANISMGROUPFAUNA"</code>.
	 * 
	 * This is a bag extractor so we have to go by type as there aren't any referring properties.
	 * 
	 * @param targetTypeLocalName	local name of entity type to perform extraction on
	 */
	public void setTargetTypeLocalName(String targetTypeLocalName) {
		this.targetTypeLocalName = targetTypeLocalName;
	}

	/**
	 * Name used for the output attribute record. e.g.: <code>height</code>
	 * 
	 * @param finalName	name used in output record
	 */
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
