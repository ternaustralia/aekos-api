package au.org.aekos.service.search.load;

import java.io.IOException;
import java.util.List;

/**
 * Push or Pull?  Be fun to query tdb direct.
 * 
 * This client will delegate to a loader service . . .
 * 
 * 
 * @author Ben
 *
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
	
	public void addSpeciesTraitTermsToIndex(String species, List<String> traits) throws IOException;
	
	public void addTraitSpeciesTermsToIndex(String trait, List<String> species) throws IOException;
	
	public void addSpeciesTraitTermToIndex(String species, String trait) throws IOException;
	
	public void addSpeciesEnvironmentTermsToIndex(String species, List<String> environmentTraits) throws IOException;
	
	public void addSpeciesEnvironmentTermToIndex(String species, String environmentTrait) throws IOException;
}
