package au.org.aekos;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitVocabEntry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "AekosV1", produces = "application/json")
@RestController
@RequestMapping("/v1")
public class AekosRecordController {

	private static final Logger logger = LoggerFactory.getLogger(AekosRecordController.class);
	
	@Autowired
	private DataFactory dataFactory;
	
	@RequestMapping(path="/getTraitVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get trait vocabulary", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitVocab(HttpServletResponse resp) {
		try {
			setCommonHeaders(resp);
			return dataFactory.getTraitVocabData();
		} catch (IOException e) {
			throw new IllegalStateException("Data error: failed to load trait data", e);
		}
	}

	@RequestMapping(path="/speciesAutocomplete.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Autocomplete partial species names", notes = "TODO", tags="Search")
    public List<SpeciesName> speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) {
		setCommonHeaders(resp);
		// TODO do we need propagating headers to enable browser side caching
		List<SpeciesName> result = new LinkedList<>();
		if (!StringUtils.hasText(partialSpeciesName)) {
			return result;
		}
		for (SpeciesName curr : dataFactory.getTraitBySpeciesData().keySet()) {
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
		Map<SpeciesName, List<TraitVocabEntry>> traitBySpeciesData = dataFactory.getTraitBySpeciesData();
		for (String curr : speciesNames) {
			List<TraitVocabEntry> traitsForCurr = traitBySpeciesData.get(new SpeciesName(curr));
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
			List<SpeciesName> speciesForCurr = dataFactory.getSpeciesByTrait().get(curr);
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
    public List<SpeciesDataRecord> speciesDataDotJson(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
		return getSpeciesDataJson(limit, resp);
	}
	
	@RequestMapping(path="/speciesData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE,
    		headers="Accept="+MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data", notes = "Gets Aekos data", tags="Data Retrieval")
    public List<SpeciesDataRecord> speciesDataJson(@RequestParam(required=false) Integer limit, HttpServletResponse resp) {
    	return getSpeciesDataJson(limit, resp);
    }

	private List<SpeciesDataRecord> getSpeciesDataJson(Integer limit, HttpServletResponse resp) {
		setCommonHeaders(resp);
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return dataFactory.getSpeciesJsonData(checkedLimit);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			throw new RuntimeException("Server failed to read data from datasource", e);
		}
	}

	@RequestMapping(path="/speciesData.csv", method=RequestMethod.GET, produces="text/csv")
    @ApiOperation(value = "Get species data in CSV format", notes = "TODO", tags="Data Retrieval")
    public void speciesDataDotCsv(@RequestParam(required=false) Integer limit,
    		@ApiParam("Makes the response trigger a downloadable file rather than streaming the response")
    			@RequestParam(required=false, defaultValue="false") boolean download,
    		@ApiIgnore Writer responseWriter, HttpServletResponse resp) {
    	getSpeciesDataCsv(limit, download, responseWriter, resp);
    }
	
    @RequestMapping(path="/speciesData", method=RequestMethod.GET, produces="text/csv", headers="Accept=text/csv")
    //FIXME what do I put in here? Do I copy from the other overloaded method?
    @ApiOperation(value = "Get species data blah", notes = "Gets Aekos data", tags="Data Retrieval")
    public void speciesDataCsv(@RequestParam(required=false) Integer limit,
    		@ApiIgnore Writer responseWriter, HttpServletResponse resp) {
    	getSpeciesDataCsv(limit, false, responseWriter, resp);
    }

	private void getSpeciesDataCsv(Integer limit, boolean download, Writer responseWriter, HttpServletResponse resp) {
		setCommonHeaders(resp);
		// TODO add col headers
    	resp.setContentType("text/csv");
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=aekosSpeciesData.csv"); // TODO give a more dynamic name
    	}
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		dataFactory.getSpeciesCsvData(checkedLimit, responseWriter);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Failed to return AEKOS data", e);
			throw new AekosApiServerException("Server failed to retrieve species CSV data", e);
		}
	}
    
    @RequestMapping(path="/traitData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all trait data for the specified species", notes = "TODO", tags="Data Retrieval")
    public List<TraitDataRecord> traitDataJson(@RequestParam(name="speciesName") String[] speciesName, 
    		@RequestParam(name="traitName") String[] traitNames, HttpServletResponse resp) {
    	setCommonHeaders(resp);
    	List<TraitDataRecord> result = new LinkedList<>();
		result.add(new TraitDataRecord("row1"));
		result.add(new TraitDataRecord("row2"));
		return result;
	}
    
    @RequestMapping(path="/environmentData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all environment data for the specified species", notes = "TODO", tags="Data Retrieval")
    public List<EnvironmentDataRecord> environmentDataJson(@RequestParam(name="speciesName") String[] speciesName,
    		@RequestParam(name="envVarName") String[] envVarNames, HttpServletResponse resp) {
    	setCommonHeaders(resp);
    	List<EnvironmentDataRecord> result = new LinkedList<>();
		result.add(new EnvironmentDataRecord("row1"));
		return result;
	}
    
	private void setCommonHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "*"); // FIXME replace with @CrossOrigin
	}
}
