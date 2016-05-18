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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.model.TraitVocabEntry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "AekosV1", description = "Aekos API", produces = "application/json")
@RestController()
@RequestMapping("/v1")
public class AekosRecordController {

	private static final Logger logger = LoggerFactory.getLogger(AekosRecordController.class);
	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
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
	@ApiOperation(value = "Autocomplete partial species names", notes = "TODO")
    public SpeciesName speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) {
		setCommonHeaders(resp);
		return new SpeciesName("testSpecies");
	}
	
	@RequestMapping(path="/getTraitsBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available traits for specified species", notes = "TODO")
    public List<TraitVocabEntry> getTraitsBySpecies(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<TraitVocabEntry> result = new ArrayList<>();
		result.add(new TraitVocabEntry("trait1", "Trait One"));
		result.add(new TraitVocabEntry("trait2", "Trait Two"));
		for (String curr : speciesNames) {
			result.add(new TraitVocabEntry("trait" + curr, "Trait " + curr));
		}
		return result;
	}
	
	@RequestMapping(path="/getSpeciesByTrait.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available species for specified traits", notes = "TODO")
    public List<SpeciesName> getSpeciesByTrait(@RequestParam(name="traitName") String[] traitNames, HttpServletResponse resp) {
		setCommonHeaders(resp);
		List<SpeciesName> result = new ArrayList<>();
		result.add(new SpeciesName("species1"));
		result.add(new SpeciesName("species2"));
		for (String curr : traitNames) {
			result.add(new SpeciesName("species" + curr));
		}
		return result;
	}
	
	@RequestMapping(path="/getEnvironmentBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available environment variable names for specified species", notes = "TODO")
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
	
    @RequestMapping(path="/speciesData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get Aekos data", notes = "Gets Aekos data")
    @ApiResponses(value = {
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Internal Server Error"),
            @ApiResponse(code = 201, message = "") })
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
    @ApiOperation(value = "Get species data in CSV format", notes = "TODO")
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
    @ApiOperation(value = "Get all trait data for the specified species", notes = "TODO")
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
}
