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
import static au.org.aekos.controller.ControllerHelper.generateDownloadFileName;

import java.io.Writer;
import java.util.Arrays;
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

@Api(description=RETRIEVAL_BY_SPECIES_DESC, produces=MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/v1")
public class ApiV1SpeciesRetrievalController {

	static final String HONOURS_SUFFIX = "peciesData.*</code> resources.";
	static final String HONOURS_PREFIX = " This resource honours <code>Accept</code> headers represented by any of the <code>/";
	private static final String HONOURS_HEADERS = HONOURS_PREFIX + "s" + HONOURS_SUFFIX;
	
	@Autowired
	private RetrievalService retrievalService;
	
	@RequestMapping(path="/speciesData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data in JSON format",
    		notes = "Gets Darwin Core records for the supplied species name(s) in JSON format.",
    		tags=DATA_RETRIEVAL_BY_SPECIES_TAG)
    public SpeciesDataResponse speciesDataDotJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		SpeciesDataResponse result = retrievalService.getSpeciesDataJson(Arrays.asList(speciesNames), start, rows);
		resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
	}
	
	@RequestMapping(path="/speciesData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE,
    		headers="Accept="+MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data",
    		notes = "Gets Darwin Core records for the supplied species name(s)" + CONTENT_NEGOTIATION_FRAGMENT + HONOURS_HEADERS,
    		produces=MediaType.APPLICATION_JSON_VALUE + ", " + TEXT_CSV_MIME, // Forcing Swagger content negotiation until support for two methods is in
    		tags=DATA_RETRIEVAL_BY_SPECIES_TAG)
    public SpeciesDataResponse speciesDataJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		return speciesDataDotJson(speciesNames, start, rows, req, resp);
    }

	@RequestMapping(path="/speciesData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get species occurrence data in CSV format",
    		notes = "Gets Darwin Core records for the supplied species name(s) in CSV format.",
    		tags=DATA_RETRIEVAL_BY_SPECIES_TAG)
    public void speciesDataDotCsv(
    		@RequestParam(name="speciesName") @ApiParam(value="Scientific name(s) of species to retrieve data for", example="Atriplex vesicaria") String[] speciesNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@RequestParam(required=false, defaultValue="false") @ApiParam(DL_PARAM_MSG) boolean download,
    		Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		resp.setContentType(TEXT_CSV_MIME);
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=" + generateDownloadFileName("species", new Date()));
    	}
		RetrievalResponseHeader header = retrievalService.getSpeciesDataCsv(Arrays.asList(speciesNames), start, rows, responseWriter);
		resp.addHeader(HttpHeaders.LINK, buildHateoasLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), header));
    }
	
    @RequestMapping(path="/speciesData", method=RequestMethod.GET, produces=TEXT_CSV_MIME, headers="Accept="+TEXT_CSV_MIME)
    // Not defining another @ApiOperation as it won't generate the expected swagger doco. Remove @ApiIgnore when fixed
    // See https://github.com/springfox/springfox/issues/1367 for more info about when this is coming.
    @ApiIgnore
    public void speciesDataCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue=DEFAULT_START) @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	boolean dontDownload = false;
		speciesDataDotCsv(speciesNames, start, rows, dontDownload, responseWriter, req, resp);
    }
}
