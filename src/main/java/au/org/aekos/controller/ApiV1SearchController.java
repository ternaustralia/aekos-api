package au.org.aekos.controller;

import java.io.IOException;
import java.util.Arrays;
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
import au.org.aekos.service.search.PageRequest;
import au.org.aekos.service.search.SearchService;
import au.org.aekos.service.search.index.SpeciesLookupIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "AekosV1", produces=MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/v1")
public class ApiV1SearchController {

	// TODO add lots more Swagger doco
	// TODO figure out how to get Swagger to support content negotiation with overloaded methods
	// TODO am I doing content negotiation correctly?
	// TODO define coord ref system
	// TODO do we accept LSID/species ID and/or a species name for the species related services?
	
	@Autowired
	private SearchService searchService;
	
	@Autowired
	private SpeciesLookupIndexService speciesSearchService;
	
	//Is this a list of all the traits?  I guess so . . .
	@RequestMapping(path="/getTraitVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get trait vocabulary", notes = "TODO", tags="Search")
    public List<TraitOrEnvironmentalVariableVocabEntry> getTraitVocab(HttpServletResponse resp) {
		return searchService.getTraitVocabData();
	}

	@RequestMapping(path="/speciesAutocomplete.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Autocomplete partial species names", notes = "TODO", tags="Search")
    public List<SpeciesName> speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) throws IOException {
		// TODO do we need propagating headers to enable browser side caching
		// TODO look at returning a complex object with meta information like total results, curr page, etc
		return speciesSearchService.performSearch(partialSpeciesName, 50, false);
	}

	@RequestMapping(path="/getTraitsBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available traits for specified species", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitsBySpecies(@RequestParam(name="speciesName", required=true) 
    												String[] speciesNames,
										    		@RequestParam(name="page", required = false)
    												int page , 
										    		@RequestParam(name="numResults", required=false) 
    												int numResults, 
										    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		return searchService.getTraitBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	@RequestMapping(path="/getSpeciesByTrait.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available species for specified traits", notes = "TODO", tags="Search")
    public List<SpeciesName> getSpeciesByTrait(@RequestParam(name="traitName") String[] traitNames, 
									    		@RequestParam(name="page", required = false)
												int page , 
									    		@RequestParam(name="numResults", required=false) 
												int numResults, 
												HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		return searchService.getSpeciesByTrait(Arrays.asList(traitNames), pagination);
	}
	
	@RequestMapping(path="/getEnvironmentBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available environment variable names for specified species", notes = "TODO", tags="Search")
    public List<TraitOrEnvironmentalVariableVocabEntry> getEnvironmentBySpecies(@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="page", required = false)
			int page , 
    		@RequestParam(name="numResults", required=false) 
			int numResults,   		
    		HttpServletResponse resp) {
		PageRequest pagination = new PageRequest(page, numResults);
		return searchService.getEnvironmentBySpecies(Arrays.asList(speciesNames), pagination);
	}
	
	//species summary document??
	@RequestMapping(path="/speciesSummary.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get a summary of the specified species", notes = "TODO", tags="Search")
    public List<SpeciesSummary> getSpeciesSummary(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		// TODO support searching/substring as the param, same as ALA
		return searchService.getSpeciesSummary(Arrays.asList(speciesNames));
	}
}
