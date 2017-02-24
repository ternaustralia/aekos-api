package au.org.aekos.api.loader.service.load;

import java.io.IOException;

/**
 * Loads records into the index to be used for searching.
 */
public interface LoaderClient {
	
	/**
	 * Initialises the IndexWriter, sets up the load process
	 */
	public void beginLoad();
	
	/**
	 * Call to commit any outstanding index changes and close the IndexWriter etc.
	 */
	public void endLoad();

	void deleteAll() throws IOException;

	void addSpeciesRecord(SpeciesLoaderRecord record) throws IOException;

	void addEnvRecord(EnvironmentLoaderRecord record) throws IOException;
}
