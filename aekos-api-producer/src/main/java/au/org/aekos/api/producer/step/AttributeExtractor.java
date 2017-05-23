package au.org.aekos.api.producer.step;

import org.apache.jena.rdf.model.Resource;

public interface AttributeExtractor {

	/**
	 * Performs the extraction.
	 * 
	 * @param subject	resource to start the search on
	 * @return			populated trait record
	 */
	AttributeRecord doExtractOn(Resource subject);

	/**
	 * Identifier of this type of TraitExtractor
	 */
	String getId();
}
