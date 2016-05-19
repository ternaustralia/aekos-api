package au.org.aekos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.model.TraitVocabEntry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "AekosV1", produces = "application/json")
@RestController()
@RequestMapping("/v1")
public class AekosRecordController {

	private static final Logger logger = LoggerFactory.getLogger(AekosRecordController.class);
	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	private final Map<SpeciesName, List<TraitVocabEntry>> traitsBySpecies = initTraitBySpecies();
	private final Map<String, List<SpeciesName>> speciesByTrait = initSpeciesByTrait();
	
	@Autowired
	private TraitDataFactory traitDataFactory;
	
	@RequestMapping(path="/getTraitVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get trait vocabulary", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitVocab(HttpServletResponse resp) {
		try {
			setCommonHeaders(resp);
			return traitDataFactory.getData();
		} catch (IOException e) {
			throw new IllegalStateException("Data error: failed to load trait data", e);
		}
	}

	@RequestMapping(path="/speciesAutocomplete.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Autocomplete partial species names", notes = "TODO", tags="Search")
    public List<SpeciesName> speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<SpeciesName> result = new LinkedList<>();
		if (!StringUtils.hasText(partialSpeciesName)) {
			return result;
		}
		for (SpeciesName curr : traitsBySpecies.keySet()) {
			if (curr.getName().toLowerCase().startsWith(partialSpeciesName.toLowerCase())) {
				result.add(curr);
			}
		}
		return result;
	}
	
	@RequestMapping(path="/getTraitsBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available traits for specified species", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitsBySpecies(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<TraitVocabEntry> result = new ArrayList<>();
		for (String curr : speciesNames) {
			List<TraitVocabEntry> traitsForCurr = traitsBySpecies.get(new SpeciesName(curr));
			if (traitsForCurr == null) {
				continue;
			}
			result.addAll(traitsForCurr);
		}
		return result;
	}
	
	@RequestMapping(path="/getSpeciesByTrait.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available species for specified traits", notes = "TODO", tags="Search")
    public List<SpeciesName> getSpeciesByTrait(@RequestParam(name="traitName") String[] traitNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<SpeciesName> result = new ArrayList<>();
		for (String curr : traitNames) {
			List<SpeciesName> speciesForCurr = speciesByTrait.get(curr);
			if (speciesForCurr == null) {
				continue;
			}
			result.addAll(speciesForCurr);
		}
		return result;
	}
	
	@RequestMapping(path="/getEnvironmentBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available environment variable names for specified species", notes = "TODO", tags="Search")
    public List<EnvironmentVariable> getEnvironmentBySpecies(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<EnvironmentVariable> result = new ArrayList<>();
		result.add(new EnvironmentVariable("soilPh_10cm", "Soil pH 10cm"));
		result.add(new EnvironmentVariable("windSpeedDirection", "Wind Speed Direction"));
		for (String curr : speciesNames) {
			result.add(new EnvironmentVariable("env" + curr, "Env " + curr));
		}
		return result;
	}
	
	@RequestMapping(path="/speciesSummary.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get a summary of the specified species", notes = "TODO", tags="Search")
    public List<SpeciesSummary> getSpeciesSummary(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		// TODO support searching/substring as the param, same as ALA
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
	
    @RequestMapping(path="/speciesData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data in JSON format", notes = "Gets Aekos data", tags="Data Retrieval")
    public SpeciesDataResponse speciesDataJson(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	setCommonHeaders(resp);
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getParsedData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return new SpeciesDataResponse(e);
		}
    }

    @RequestMapping(path="/speciesData.csv", method=RequestMethod.GET, produces=MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Get species data in CSV format", notes = "TODO", tags="Data Retrieval")
    public String speciesDataCsv(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	setCommonHeaders(resp);
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getRawData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			return "Server Error: [" + e.getClass().toString() + "] " + e.getMessage();
		}
    }
    
    @RequestMapping(path="/traitData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all trait data for the specified species", notes = "TODO", tags="Data Retrieval")
    public TraitDataResponse traitDataJson(@RequestParam String speciesName, HttpServletResponse resp) {
    	setCommonHeaders(resp);
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
	
	private void setCommonHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "*"); // FIXME replace with @CrossOrigin
	}
	
	private Map<SpeciesName, List<TraitVocabEntry>> initTraitBySpecies() {
		Map<SpeciesName, List<TraitVocabEntry>> result = new HashMap<>();
		result.put(new SpeciesName("Leersia hexandra"), traitList("Life Stage","Dominance","Total Length"));
		result.put(new SpeciesName("Ectrosia schultzii var. annua"), traitList("Life Stage","Basal Area"));
		result.put(new SpeciesName("Rutaceae sp."), traitList("Height","Biomass"));
		result.put(new SpeciesName("Tristania neriifolia"), traitList("Weight","Canopy Cover"));
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
	
	private Map<String, List<SpeciesName>> initSpeciesByTrait() {
		Map<String, List<SpeciesName>> result = new HashMap<>();
		for (Entry<SpeciesName, List<TraitVocabEntry>> currEntry : initTraitBySpecies().entrySet()) {
			SpeciesName speciesName = currEntry.getKey();
			for (TraitVocabEntry currTrait : currEntry.getValue()) {
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
}
