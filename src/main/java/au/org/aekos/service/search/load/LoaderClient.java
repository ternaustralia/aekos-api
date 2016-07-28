package au.org.aekos.service.search.load;

import java.io.IOException;
import java.util.List;

import au.org.aekos.service.retrieval.IndexLoaderRecord;

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
	
	public void addSpeciesTraitTermsToIndex(String species, List<String> traits) throws IOException;
	
	public void addTraitSpeciesTermsToIndex(String trait, List<String> species) throws IOException;
	
	public void addSpeciesTraitTermToIndex(String species, String trait) throws IOException;
	
	public void addSpeciesEnvironmentTermsToIndex(String species, List<String> environmentTraits) throws IOException;
	
	public void addSpeciesEnvironmentTermToIndex(String species, String environmentTrait) throws IOException;
	
	/**
	 * Adds an occurrence of a species name to be used in the speciesAutocomplete.
	 * 
	 * @param record	record to extract species name from
	 */
	public void addSpecies(IndexLoaderRecord record) throws IOException;
}
