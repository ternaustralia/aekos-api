package au.org.aekos.service.search;

import java.util.List;

import au.org.aekos.model.EnvironmentDataRecord.EnvironmentalVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;

public interface SearchService {

	/**
	 * Gets the whole trait vocabulary.
	 * 
	 * @return
	 */
	List<TraitVocabEntry> getTraitVocabData();

	/**
	 * Looks up the traits that have data recorded for the supplied species names.
	 * 
	 * The values of these traits are NOT returned, this is only to know that
	 * species X does have a value recorded for trait Y.
	 * 
	 * @param speciesNames 	species to look up traits for
	 * @return				list of traits available for the supplied species
	 */
	List<TraitVocabEntry> getTraitBySpecies(List<String> speciesNames);

	/**
	 * Looks up the environment variables that have data recorded for the supplied species names.
	 * 
	 * The values of these environment variables are NOT returned, this is only to know that
	 * species X does have a value recorded for environment variable Z. More accurately,
	 * species X occurs at site Y, which has environment variable Z.
	 * 
	 * @param speciesNames 	species to look up environment variables for
	 * @return				list of environment variables available for the supplied species
	 */
	List<EnvironmentalVariable> getEnvironmentBySpecies(List<String> speciesNames);
	
	/**
	 * Looks up the species that have data recorded for the supplied trait names.
	 * 
	 * @param traitNames	trait names to search for
	 * @return				species names that have data recorded for the supplied traits
	 */
	List<SpeciesName> getSpeciesByTrait(List<String> traitNames);

	/**
	 * Finds possible species name matches for the supplied partial name.
	 * 
	 * @param partialSpeciesName	partial string to match
	 * @return						list of posssible matches ordered with the most likely match first
	 */
	List<SpeciesName> autocompleteSpeciesName(String partialSpeciesName);

	/**
	 * Gets a summary of each supplied species.
	 * 
	 * A summary explains what we (the system) know about that species.
	 * Information such as how many records we hold, a URL for an image,
	 * a URL for an information page in our portal, etc.
	 * 
	 * TODO how do we deal with the case when we don't have a 1:1 (missing summaries or multiple)?
	 * 
	 * @param speciesNames	species to retrieve a summary for
	 * @return				list of summaries in the same order as species were supplied
	 */
	List<SpeciesSummary> getSpeciesSummary(List<String> speciesNames);
}
