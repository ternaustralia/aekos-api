package au.org.aekos.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.retrieval.RetrievalService;
import au.org.aekos.service.search.PageRequest;
import au.org.aekos.service.search.SearchService;
import au.org.aekos.service.search.index.SpeciesLookupIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description="Find species, traits and environmental variables", produces=MediaType.APPLICATION_JSON_VALUE, tags="Search")
@RestController
@RequestMapping("/v1")
public class ApiV1SearchController {

	// TODO add lots more Swagger doco
	// TODO figure out how to get Swagger to support content negotiation with overloaded methods
	// TODO define coord ref system
	// TODO do we accept LSID/species ID and/or a species name for the species related services?
	
	@Autowired
	private SearchService searchService;
	
	@Autowired
	private SpeciesLookupIndexService speciesSearchService;
	
	@Autowired
	private RetrievalService retrievalService;
	
	@RequestMapping(path="/getTraitVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get trait vocabulary",
		notes = "Gets a distinct list of all the traits that appear in the system. The code and label are "
				+ "supplied for each trait. The codes are required to use as parameters for other resources "
				+ "and the label information is useful for creating UIs.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getTraitVocab(HttpServletResponse resp) {
		return searchService.getTraitVocabData();
	}

	@RequestMapping(path="/getEnvironmentalVariableVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get environmental variable vocabulary",
			notes = "Gets a distinct list of all the environmental variables that appear in the system. The code and label are "
					+ "supplied for each variable. The codes are required to use as parameters for other resources "
					+ "and the label information is useful for creating UIs.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentalVariableVocab(HttpServletResponse resp) {
		return searchService.getEnvironmentalVariableVocabData();
	}
	
	@RequestMapping(path="/speciesAutocomplete.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Autocomplete partial species names",
			notes = "Performs an autocomplete on the partial species name supplied. Results starting with the supplied fragment"
					+ "will be returned ordered by most relevant.")
    public List<SpeciesName> speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) throws IOException {
		// TODO do we need propagating headers to enable browser side caching
		// TODO look at returning a complex object with meta information like total results, curr page, etc
		return speciesSearchService.performSearch(partialSpeciesName, 50, false);
	}

	@RequestMapping(path="/getTraitsBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available traits for specified species",
			notes = "Finds the traits that the supplied species have. Note that the result doesn't include the value"
					+ "of the traits, it only shows that the supplied species have values for those traits. To get the"
					+ "values, you need to use the Data Retrieval services.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getTraitsBySpecies(
    		@RequestParam(name="speciesName", required=true) String[] speciesNames,
    		@RequestParam(name="page", required = false, defaultValue="0") int page,
    		@RequestParam(name="numResults", required=false, defaultValue="20") int numResults,
    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		
		return searchService.getTraitBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	@RequestMapping(path="/getSpeciesByTrait.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available species for specified traits",
			notes = "Finds the species names that the supplied trait(s). Note that the result only shows that the "
					+ "supplied traits have species records present in the system. To get the values, you need "
					+ "to use the Data Retrieval services.")
    public List<SpeciesName> getSpeciesByTrait(
    		@RequestParam(name="traitName") String[] traitNames,
    		@RequestParam(name="page", required = false, defaultValue="0") int page,
    		@RequestParam(name="numResults", required=false, defaultValue="20") int numResults,
			HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		return searchService.getSpeciesByTrait(Arrays.asList(traitNames), pagination);
	}
	
	@RequestMapping(path="/getEnvironmentBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available environment variable names for specified species",
			notes = "Finds the environmental variables that the supplied species have. Note that the result doesn't include the value"
					+ "of the environmental variables, it only shows that the supplied species have values for those variable. To get the"
					+ "values, you need to use the Data Retrieval services.")
    public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="page", required = false, defaultValue="0") int page,
    		@RequestParam(name="numResults", required=false, defaultValue="20") int numResults,
    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		return searchService.getEnvironmentBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	@RequestMapping(path="/speciesSummary.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get a summary of the specified species",
			notes = "A summary of the information that the system holds on the supplied species name(s) "
					+ "including a count of records.")
    public List<SpeciesSummary> getSpeciesSummary(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		// TODO support searching/substring as the param, same as ALA
		List<SpeciesSummary> result = new LinkedList<>();
		for (String curr : speciesNames) {
			int recordsHeld = retrievalService.getTotalRecordsHeldForSpeciesName(curr);
			result.add(new SpeciesSummary(String.valueOf(curr.hashCode()), curr, recordsHeld));
		}
		return result;
	}
}
