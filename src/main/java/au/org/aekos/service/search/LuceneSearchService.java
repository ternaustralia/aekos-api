package au.org.aekos.service.search;

import java.util.List;

import org.springframework.stereotype.Service;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;

@Service
public class LuceneSearchService implements SearchService {

	// TODO Ben to implement
	
	@Override
	public List<TraitVocabEntry> getTraitVocabData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TraitVocabEntry> getTraitBySpecies(List<String> speciesNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnvironmentVariable> getEnvironmentBySpecies(List<String> speciesNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SpeciesName> autocompleteSpeciesName(String partialSpeciesName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SpeciesSummary> getSpeciesSummary(List<String> speciesNames) {
		// TODO Auto-generated method stub
		return null;
	}
}
