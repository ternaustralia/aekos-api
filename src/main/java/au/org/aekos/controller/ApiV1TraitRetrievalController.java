package au.org.aekos.controller;

import static au.org.aekos.controller.ControllerHelper.CONTENT_NEGOTIATION_FRAGMENT;
import static au.org.aekos.controller.ControllerHelper.DATA_RETRIEVAL_BY_SPECIES_TAG;
import static au.org.aekos.controller.ControllerHelper.DEFAULT_ROWS;
import static au.org.aekos.controller.ControllerHelper.DEFAULT_START;
import static au.org.aekos.controller.ControllerHelper.DL_PARAM_MSG;
import static au.org.aekos.controller.ControllerHelper.RETRIEVAL_BY_SPECIES_DESC;
import static au.org.aekos.controller.ControllerHelper.TEXT_CSV_MIME;
import static au.org.aekos.controller.ControllerHelper.buildHateoasLinkHeader;
import static au.org.aekos.controller.ControllerHelper.extractFullUrl;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.service.retrieval.RetrievalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@Api(description=RETRIEVAL_BY_SPECIES_DESC, produces=MediaType.APPLICATION_JSON_VALUE, tags=DATA_RETRIEVAL_BY_SPECIES_TAG)
@RestController
@RequestMapping("/v1")
public class ApiV1TraitRetrievalController {

	private static final String TRAIT_FILTERING_FRAGMENT = " If you supply "
						+ "trait names then the result records will have the traits filtered down to only the traits "
						+ "you've asked for, otherwise all traits are returned.";

	@Autowired
	private RetrievalService retrievalService;
	
    @RequestMapping(path="/traitData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get trait data in JSON format",
			notes = "Gets Darwin Core records with added trait information in JSON format." + TRAIT_FILTERING_FRAGMENT)
    public TraitDataResponse traitDataDotJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	List<String> traits = traitNames != null ? Arrays.asList(traitNames) : Collections.emptyList();
    	// TODO validate start ! < 0
    	// TODO validate count > 0
    	TraitDataResponse result = retrievalService.getTraitDataJson(Arrays.asList(speciesNames), traits, start, rows);
    	resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
    }

    @RequestMapping(path="/traitData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get trait data",
    		notes = "Gets Darwin Core records with added trait information for the supplied "
    				+ "species name(s)" + CONTENT_NEGOTIATION_FRAGMENT + TRAIT_FILTERING_FRAGMENT
    				+ " This resource honours <code>Accept</code> headers represented by any of the <code>/traitData.*</code> resources.",
			produces=MediaType.APPLICATION_JSON_VALUE + ", " + TEXT_CSV_MIME) // Forcing Swagger content negotiation until support for two methods is in)
    public TraitDataResponse traitDataJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		return traitDataDotJson(speciesNames, traitNames, start, rows, req, resp);
	}
    
    @RequestMapping(path="/traitData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get trait data in CSV format",
    		notes = "Gets Darwin Core records with added trait information in CSV format." + TRAIT_FILTERING_FRAGMENT)
    public void traitDataDotCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@RequestParam(required=false, defaultValue="false") @ApiParam(DL_PARAM_MSG) boolean download,
    		HttpServletRequest req, HttpServletResponse resp, Writer responseWriter) throws AekosApiRetrievalException {
    	resp.setContentType(TEXT_CSV_MIME);
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=aekosTraitData.csv"); // TODO give a more dynamic name
    	}
    	List<String> traits = traitNames != null ? Arrays.asList(traitNames) : Collections.emptyList();
    	// TODO validate start ! < 0
    	// TODO validate count > 0
    	String fullReqUrl = extractFullUrl(req);
    	RetrievalResponseHeader header = retrievalService.getTraitDataCsv(Arrays.asList(speciesNames), traits, start, rows, responseWriter);
    	resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(fullReqUrl), header));
    }
    
    @RequestMapping(path="/traitData", method=RequestMethod.GET, produces=TEXT_CSV_MIME, headers="Accept="+TEXT_CSV_MIME)
    // Not defining another @ApiOperation as it won't generate the expected swagger doco. Remove @ApiIgnore when fixed
    // See https://github.com/springfox/springfox/issues/1367 for more info about when this is coming.
    @ApiIgnore
    public void traitDataCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp, Writer responseWriter) throws AekosApiRetrievalException {
    	traitDataDotCsv(speciesNames, traitNames, start, rows, false, req, resp, responseWriter);
    }
}
