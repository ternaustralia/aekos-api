package au.org.aekos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.TraitVocabEntry;

@Service
public class DataFactory {

	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	private Map<SpeciesName, List<TraitVocabEntry>> traitBySpecies;
	private List<TraitVocabEntry> traitVocabs;
	private Map<String, List<SpeciesName>> speciesByTrait;
	private Map<SpeciesName, List<EnvironmentVariable>> environmentBySpecies;
	
	@Value("${data-file.trait-vocab}")
	private String dataFilePath;
	
//	@Value("${data-file.species}")
//	private String speciesDataFilePath;
//	
//	@Value("${data-file.trait}")
//	private String traitDataFilePath;
//	
//	@Value("${data-file.env}")
//	private String envDataFilePath;
	
	public List<TraitVocabEntry> getTraitVocabData() throws IOException {
		if (traitVocabs == null) {
			traitVocabs = initTraitVocabs();
		}
		return traitVocabs;
	}
	
	public Map<SpeciesName, List<TraitVocabEntry>> getTraitBySpeciesData() {
		if (traitBySpecies == null) {
			traitBySpecies = initTraitBySpecies();
		}
		return traitBySpecies;
	}
	
	public Map<String, List<SpeciesName>> getSpeciesByTrait() {
		if (speciesByTrait == null) {
			speciesByTrait = initSpeciesByTrait();
		}
		return speciesByTrait;
	}
	
	public Map<SpeciesName, List<EnvironmentVariable>> getEnvironmentBySpecies() {
		if (environmentBySpecies == null) {
			environmentBySpecies = initEnvironmentBySpecies();
		}
		return environmentBySpecies;
	}

	public void getSpeciesCsvData(int limit, Writer responseWriter) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv")));
		String currLine;
		int dontCountHeader = -1;
		int lineCounter = dontCountHeader;
		while (lineCounter < limit && (currLine = in.readLine()) != null) {
			responseWriter.write(replaceDatePlaceholder(currLine) + "\n");
			responseWriter.flush(); // TODO is it efficient to flush every row?
			lineCounter++;
		}
	}
	
	private String replaceDatePlaceholder(String line) {
		if (!line.contains(DATE_PLACEHOLDER)) {
			return line;
		}
		return line.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
	}
	
	public List<SpeciesDataRecord> getSpeciesJsonData(int limit) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv"))));
		reader.readNext(); // Bin the header
		String[] currLine;
		int lineCounter = 0;
		List<SpeciesDataRecord> result = new LinkedList<>();
		while (lineCounter < limit && (currLine = reader.readNext()) != null) {
			String[] processedLine = replaceDatePlaceholder(currLine);
			result.add(SpeciesDataRecord.deserialiseFrom(processedLine));
			lineCounter++;
		}
		reader.close();
		return result;
	}

	private String[] replaceDatePlaceholder(String[] line) {
		for (int i = 0; i<line.length;i++) {
			String currField = line[i];
			if (currField.contains(DATE_PLACEHOLDER)) {
				line[i] = currField.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
			}
		}
		return line;
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
	
	private Map<SpeciesName, List<EnvironmentVariable>> initEnvironmentBySpecies() {
		Map<SpeciesName, List<EnvironmentVariable>> result = new HashMap<>();
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
		private List<EnvironmentVariable> envList;

		public Data(List<TraitVocabEntry> traitList, List<EnvironmentVariable> envList) {
			this.traitList = traitList;
			this.envList = envList;
		}
	}
	
	private List<EnvironmentVariable> envList(String...envLabels) {
		List<EnvironmentVariable> result = new ArrayList<>();
		for (String curr : envLabels) {
			String noSpaces = curr.replaceAll("\\s", "");
			String code = noSpaces.substring(0, 1).toLowerCase() + noSpaces.substring(1);
			result.add(new EnvironmentVariable(code, curr));
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
