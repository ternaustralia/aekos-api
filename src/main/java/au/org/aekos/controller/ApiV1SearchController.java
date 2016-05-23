package au.org.aekos.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.org.aekos.model.EnvironmentVariable;
import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitVocabEntry;
import au.org.aekos.service.search.SearchService;
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
	@Qualifier("stubSearchService")
	private SearchService searchService;
	
	@RequestMapping(path="/getTraitVocab.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get trait vocabulary", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitVocab(HttpServletResponse resp) {
		return searchService.getTraitVocabData();
	}

	@RequestMapping(path="/speciesAutocomplete.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Autocomplete partial species names", notes = "TODO", tags="Search")
    public List<SpeciesName> speciesAutocomplete(@RequestParam(name="q") String partialSpeciesName, HttpServletResponse resp) {
		// TODO do we need propagating headers to enable browser side caching
		// TODO look at returning a complex object with meta information like total results, curr page, etc
		return searchService.autocompleteSpeciesName(partialSpeciesName);
	}

	@RequestMapping(path="/getTraitsBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available traits for specified species", notes = "TODO", tags="Search")
    public List<TraitVocabEntry> getTraitsBySpecies(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		return searchService.getTraitBySpecies(Arrays.asList(speciesNames));
	}
	
	@RequestMapping(path="/getSpeciesByTrait.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available species for specified traits", notes = "TODO", tags="Search")
    public List<SpeciesName> getSpeciesByTrait(@RequestParam(name="traitName") String[] traitNames, HttpServletResponse resp) {
		return searchService.getSpeciesByTrait(Arrays.asList(traitNames));
	}
	
	@RequestMapping(path="/getEnvironmentBySpecies.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get all available environment variable names for specified species", notes = "TODO", tags="Search")
    public List<EnvironmentVariable> getEnvironmentBySpecies(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		return searchService.getEnvironmentBySpecies(Arrays.asList(speciesNames));
	}
	
	@RequestMapping(path="/speciesSummary.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get a summary of the specified species", notes = "TODO", tags="Search")
    public List<SpeciesSummary> getSpeciesSummary(@RequestParam(name="speciesName") String[] speciesNames, HttpServletResponse resp) {
		// TODO support searching/substring as the param, same as ALA
		return searchService.getSpeciesSummary(Arrays.asList(speciesNames));
	}
}
