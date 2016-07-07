package au.org.aekos.service.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentDataRecord.EnvironmentalVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;

@Service
public class StubSearchService implements SearchService {

	private static final Logger logger = LoggerFactory.getLogger(StubSearchService.class);
	private Map<SpeciesName, List<TraitVocabEntry>> traitBySpecies;
	private List<TraitVocabEntry> traitVocabs;
	private Map<String, List<SpeciesName>> speciesByTrait;
	private Map<SpeciesName, List<EnvironmentalVariable>> environmentBySpecies;
	
	@Value("${data-file.trait-vocab}")
	private String dataFilePath;
	
	@Override
	public List<TraitVocabEntry> getTraitVocabData() {
		try {
			return getTraitVocabDataHelper();
		} catch (IOException e) {
			throw new IllegalStateException("Data error: failed to load trait data", e);
		}
	}
	
	@Override
	public List<TraitVocabEntry> getTraitBySpecies(List<String> speciesNames) {
		List<TraitVocabEntry> result = new ArrayList<>();
		Map<SpeciesName, List<TraitVocabEntry>> traitBySpeciesData = getTraitBySpeciesHelper();
		for (String curr : speciesNames) {
			List<TraitVocabEntry> traitsForCurr = traitBySpeciesData.get(new SpeciesName(curr));
			if (traitsForCurr == null) {
				continue;
			}
			result.addAll(traitsForCurr);
		}
		return result;
	}

	@Override
	public List<SpeciesName> getSpeciesByTrait(List<String> traitNames) {
		List<SpeciesName> result = new ArrayList<>();
		for (String curr : traitNames) {
			List<SpeciesName> speciesForCurr = getSpeciesByTraitHelper().get(curr);
			if (speciesForCurr == null) {
				continue;
			}
			result.addAll(speciesForCurr);
		}
		return result;
	}
	
	@Override
	public List<EnvironmentalVariable> getEnvironmentBySpecies(List<String> speciesNames) {
		List<EnvironmentalVariable> result = new ArrayList<>();
		for (String curr : speciesNames) {
			List<EnvironmentalVariable> envForCurr = getEnvironmentBySpeciesHelper().get(new SpeciesName(curr));
			if (envForCurr == null) {
				continue;
			}
			result.addAll(envForCurr);
		}
		return result;
	}
	
	@Override
	public List<SpeciesName> autocompleteSpeciesName(String partialSpeciesName) {
		List<SpeciesName> result = new LinkedList<>();
		if (!StringUtils.hasText(partialSpeciesName)) {
			return result;
		}
		for (SpeciesName curr : getTraitBySpeciesHelper().keySet()) {
			if (curr.getName().toLowerCase().startsWith(partialSpeciesName.toLowerCase())) {
				result.add(curr);
			}
		}
		return result;
	}

	@Override
	public List<SpeciesSummary> getSpeciesSummary(List<String> speciesNames) {
		List<SpeciesSummary> result = new ArrayList<>();
//		RestTemplate rt = new RestTemplate();
		for (String curr : speciesNames) {
			// TODO look at getting info from ALA
//			String jsonResp = rt.getForObject("http://bie.ala.org.au/ws/search.json?q=" + URLEncoder.encode(curr, "utf8"), String.class);
//			JSONObject obj = new JSONObject(jsonResp);
//			JSONObject firstResult = obj.getJSONObject("searchResults").getJSONArray("results").getJSONObject(1);
			try {
				result.add(new SpeciesSummary(new SpeciesName(curr).getId(), curr, "science " + curr,
						123, new URL("http://ecoinformatics.org.au/sites/default/files/TERN189x80.png"), // FIXME get image URL from ALA
						new URL("http://aekos.org.au/FIXME"), // FIXME create and then link to landing page
						"species")); // FIXME how do we find out class?
			} catch (MalformedURLException e) {
				logger.error("Data error: failed to create URL", e);
			}
		}
		return result;
	}
	
	private List<TraitVocabEntry> getTraitVocabDataHelper() throws IOException {
		if (traitVocabs == null) {
			traitVocabs = initTraitVocabs();
		}
		return traitVocabs;
	}
	
	private Map<SpeciesName, List<TraitVocabEntry>> getTraitBySpeciesHelper() {
		if (traitBySpecies == null) {
			traitBySpecies = initTraitBySpecies();
		}
		return traitBySpecies;
	}
	
	private Map<String, List<SpeciesName>> getSpeciesByTraitHelper() {
		if (speciesByTrait == null) {
			speciesByTrait = initSpeciesByTrait();
		}
		return speciesByTrait;
	}
	
	private Map<SpeciesName, List<EnvironmentalVariable>> getEnvironmentBySpeciesHelper() {
		if (environmentBySpecies == null) {
			environmentBySpecies = initEnvironmentBySpecies();
		}
		return environmentBySpecies;
	}

	private List<TraitVocabEntry> initTraitVocabs() throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(dataFilePath))));
		String[] currLine;
		List<TraitVocabEntry> result = new LinkedList<>();
		while ((currLine = reader.readNext()) != null) {
			result.add(TraitVocabEntry.deserialiseFrom(currLine));
		}
		reader.close();
		return result;
	}
	
	private Map<SpeciesName, List<TraitVocabEntry>> initTraitBySpecies() {
		Map<SpeciesName, List<TraitVocabEntry>> result = new HashMap<>();
		for (Entry<SpeciesName, Data> currEntry : initMainData().entrySet()) {
			SpeciesName speciesName = currEntry.getKey();
			result.put(speciesName, currEntry.getValue().traitList);
		}
		return result;
	}

	private Map<String, List<SpeciesName>> initSpeciesByTrait() {
		Map<String, List<SpeciesName>> result = new HashMap<>();
		for (Entry<SpeciesName, Data> currEntry : initMainData().entrySet()) {
			SpeciesName speciesName = currEntry.getKey();
			for (TraitVocabEntry currTrait : currEntry.getValue().traitList) {
				List<SpeciesName> speciesList = result.get(currTrait);
				if (speciesList == null) {
					speciesList = new ArrayList<>();
					result.put(currTrait.getCode(), speciesList);
				}
				speciesList.add(speciesName);
			}
		}
		return result;
	}
	
	private Map<SpeciesName, List<EnvironmentalVariable>> initEnvironmentBySpecies() {
		Map<SpeciesName, List<EnvironmentalVariable>> result = new HashMap<>();
		for (Entry<SpeciesName, Data> currEntry : initMainData().entrySet()) {
			SpeciesName speciesName = currEntry.getKey();
			result.put(speciesName, currEntry.getValue().envList);
		}
		return result;
	}
	
	private Map<SpeciesName, Data> initMainData() {
		Map<SpeciesName, Data> result = new HashMap<>();
		result.put(new SpeciesName("Leersia hexandra"), new Data(traitList("Life Stage","Dominance","Total Length"), envList("Soil pH 10cm")));
		result.put(new SpeciesName("Ectrosia schultzii var. annua"), new Data(traitList("Life Stage","Basal Area"), envList("Soil pH 20cm")));
		result.put(new SpeciesName("Rutaceae sp."), new Data(traitList("Height","Biomass"), envList("Soil pH 10cm", "Wind Speed Direction")));
		result.put(new SpeciesName("Tristania neriifolia"), new Data(traitList("Weight","Canopy Cover"), envList("Wind Speed Direction", "Soil pH 20cm")));
		return result;
	}
	
	private class Data {
		private List<TraitVocabEntry> traitList;
		private List<EnvironmentalVariable> envList;

		public Data(List<TraitVocabEntry> traitList, List<EnvironmentalVariable> envList) {
			this.traitList = traitList;
			this.envList = envList;
		}
	}
	
	private List<EnvironmentalVariable> envList(String...envLabels) {
		List<EnvironmentalVariable> result = new ArrayList<>();
		for (String curr : envLabels) {
			String noSpaces = curr.replaceAll("\\s", "");
			String code = noSpaces.substring(0, 1).toLowerCase() + noSpaces.substring(1);
			result.add(new EnvironmentalVariable(code, curr, "FIXME"));
		}
		return result;
	}

	private List<TraitVocabEntry> traitList(String...traitNames) {
		List<TraitVocabEntry> result = new ArrayList<>();
		for (String curr : traitNames) {
			String noSpaces = curr.replaceAll("\\s", "");
			String code = noSpaces.substring(0, 1).toLowerCase() + noSpaces.substring(1);
			result.add(new TraitVocabEntry(code, curr));
		}
		return result;
	}
}
