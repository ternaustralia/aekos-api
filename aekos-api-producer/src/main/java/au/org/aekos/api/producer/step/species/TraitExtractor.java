package au.org.aekos.api.producer.step.species;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.step.species.out.TraitRecord;

public interface TraitExtractor {

	/**
	 * Performs the extraction.
	 * 
	 * @param subject	resource to start the search on
	 * @param parentId	ID of the parent species record in the output
	 * @return			populated trait record
	 */
	TraitRecord doExtractOn(Resource subject, String parentId);

	/**
	 * Identifier of this type of TraitExtractor
	 */
	String getId();
}
