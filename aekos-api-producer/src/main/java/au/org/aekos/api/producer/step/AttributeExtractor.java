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

	/**
	 * Check if this extractor can perform an extraction on the supplied subject.
	 * 
	 * @param subject	subject to check
	 * @return			<code>true</code> if an extraction can be performed, <code>false</code> otherwise
	 */
	boolean canHandle(Resource subject);
}
