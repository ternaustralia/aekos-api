package au.org.aekos.util;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

public interface AekosJenaModelFactory {

	/**
	 * Get the singleton instance of the model
	 * 
	 * @return	singleton model instance
	 */
	Model getInstance();

	/**
	 * Get the containing dataset instance.
	 * 
	 * @return	dataset that contains the model
	 */
	Dataset getDatasetInstance();
}
