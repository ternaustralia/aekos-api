package au.org.aekos.controller;

import static au.org.aekos.controller.ApiV1SpeciesRetrievalController.HONOURS_PREFIX;
import static au.org.aekos.controller.ApiV1SpeciesRetrievalController.HONOURS_SUFFIX;
import static au.org.aekos.controller.ControllerHelper.CONTENT_NEGOTIATION_FRAGMENT;
import static au.org.aekos.controller.ControllerHelper.DATA_RETRIEVAL_ALL_TAG;
import static au.org.aekos.controller.ControllerHelper.DEFAULT_ROWS;
import static au.org.aekos.controller.ControllerHelper.DEFAULT_START;
import static au.org.aekos.controller.ControllerHelper.DL_PARAM_MSG;
import static au.org.aekos.controller.ControllerHelper.RETRIEVAL_ALL_DESC;
import static au.org.aekos.controller.ControllerHelper.TEXT_CSV_MIME;
import static au.org.aekos.controller.ControllerHelper.buildHateoasLinkHeader;
import static au.org.aekos.controller.ControllerHelper.extractFullUrl;
import static au.org.aekos.controller.ControllerHelper.generateDownloadFileName;

import java.io.Writer;
import java.util.Date;

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

import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.service.retrieval.RetrievalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@Api(description=RETRIEVAL_ALL_DESC, produces=MediaType.APPLICATION_JSON_VALUE, tags=DATA_RETRIEVAL_ALL_TAG)
@RestController
@RequestMapping("/v1")
public class ApiV1AllSpeciesRetrievalController {

	private static final String ALL_HONOURS_HEADERS = HONOURS_PREFIX + "allS" + HONOURS_SUFFIX;
	
	@Autowired
	private RetrievalService retrievalService;
	
    @RequestMapping(path="/allSpeciesData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all species occurrence data in JSON format",
    		notes = "Gets all Darwin Core records in JSON format.")
    public SpeciesDataResponse allSpeciesDataDotJson(
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		SpeciesDataResponse result = retrievalService.getAllSpeciesDataJson(start, rows);
		resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
	}
    
    @RequestMapping(path="/allSpeciesData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE,
    		headers="Accept="+MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all species data",
    		notes = "Gets all Darwin Core records" + CONTENT_NEGOTIATION_FRAGMENT + ALL_HONOURS_HEADERS,
    		produces=MediaType.APPLICATION_JSON_VALUE + ", " + TEXT_CSV_MIME) // Forcing Swagger content negotiation until support for two methods is in
    public SpeciesDataResponse allSpeciesDataJson(
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		return allSpeciesDataDotJson(start, rows, req, resp);
    }

	@RequestMapping(path="/allSpeciesData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get all species occurrence data in CSV format",
    		notes = "Gets all Darwin Core records in CSV format.")
    public void allSpeciesDataDotCsv(
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@RequestParam(required=false, defaultValue="false") @ApiParam(DL_PARAM_MSG) boolean download,
    		Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		resp.setContentType(TEXT_CSV_MIME);
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=" + generateDownloadFileName("species", new Date()));
    	}
		RetrievalResponseHeader header = retrievalService.getAllSpeciesDataCsv(start, rows, responseWriter);
		resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), header));
    }
	
    @RequestMapping(path="/allSpeciesData", method=RequestMethod.GET, produces=TEXT_CSV_MIME, headers="Accept="+TEXT_CSV_MIME)
    // Not defining another @ApiOperation as it won't generate the expected swagger doco. Remove @ApiIgnore when fixed
    // See https://github.com/springfox/springfox/issues/1367 for more info about when this is coming.
    @ApiIgnore
    public void allSpeciesDataCsv(
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	boolean dontDownload = false;
		allSpeciesDataDotCsv(start, rows, dontDownload, responseWriter, req, resp);
    }
}
