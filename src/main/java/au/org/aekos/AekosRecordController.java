package au.org.aekos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;

import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.model.TraitVocabEntry;

@RestController()
@RequestMapping("/v1")
public class AekosRecordController {

	private static final Logger logger = LoggerFactory.getLogger(AekosRecordController.class);
	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@RequestMapping("/getTraitVocab.json")
    public TraitVocabEntry getTraitVocab(HttpServletResponse resp) {
		return new TraitVocabEntry("testTrait");
	}
	
	@RequestMapping("/speciesAutocomplete.json")
    public SpeciesName speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) {
		return new SpeciesName("testSpecies");
	}
	
	@RequestMapping("/getTraitsBySpecies.json")
    public List<TraitVocabEntry> getTraitsBySpecies(@RequestParam String speciesName, HttpServletResponse resp) {
		List<TraitVocabEntry> result = new ArrayList<>();
		result.add(new TraitVocabEntry("trait1"));
		result.add(new TraitVocabEntry("trait2"));
		result.add(new TraitVocabEntry("trait" + speciesName));
		return result;
	}
	
	@RequestMapping("/getSpeciesByTrait.json")
    public List<SpeciesName> getSpeciesByTrait(@RequestParam String traitName, HttpServletResponse resp) {
		List<SpeciesName> result = new ArrayList<>();
		result.add(new SpeciesName("species1"));
		result.add(new SpeciesName("species2"));
		result.add(new SpeciesName("species" + traitName));
		return result;
	}
	
    @RequestMapping("/speciesData.json")
    public SpeciesDataResponse speciesDataJson(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getParsedData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return new SpeciesDataResponse(e);
		}
    }

    @RequestMapping("/speciesData.csv")
    public String speciesDataCsv(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getRawData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return "Server Error: [" + e.getClass().toString() + "] " + e.getMessage();
		}
    }
    
    @RequestMapping("/traitData.json")
    public TraitDataResponse traitDataJson(@RequestParam String speciesName, HttpServletResponse resp) {
		TraitDataResponse result = new TraitDataResponse();
		result.add(new TraitDataRecord("row1"));
		result.add(new TraitDataRecord("row2"));
		return result;
	}
    
	private String getRawData(int limit) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv")));
		in.readLine(); // Bin the header
		String currLine;
		int lineCounter = 0;
		StringBuilder result = new StringBuilder();
		while (lineCounter < limit && (currLine = in.readLine()) != null) {
			result.append(replaceDatePlaceholder(currLine) + "\n");
			lineCounter++;
		}
		return result.toString();
	}
	
	private SpeciesDataResponse getParsedData(int limit) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv"))));
		reader.readNext(); // Bin the header
		String[] currLine;
		int lineCounter = 0;
		SpeciesDataResponse result = new SpeciesDataResponse();
		while (lineCounter < limit && (currLine = reader.readNext()) != null) {
			String[] processedLine = replaceDatePlaceholder(currLine);
			result.addData(SpeciesDataRecord.deserialiseFrom(processedLine));
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
	
	private String replaceDatePlaceholder(String line) {
		if (!line.contains(DATE_PLACEHOLDER)) {
			return line;
		}
		return line.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
	}
}
