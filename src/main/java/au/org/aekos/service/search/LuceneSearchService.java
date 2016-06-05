package au.org.aekos.service.search;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;
import au.org.aekos.service.search.index.SpeciesLookupIndexService;

@Service
public class LuceneSearchService implements SearchService {

	// TODO Ben to implement
	@Autowired
	private SpeciesLookupIndexService speciesSearchService;
	
	
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
		try {
			return speciesSearchService.performSearch(partialSpeciesName, 50, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
