package au.org.aekos.controller;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import au.org.aekos.model.AbstractResponse;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.service.retrieval.RetrievalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "AekosV1", produces=MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/v1")
public class ApiV1RetrievalController {

	private static final String DEFAULT_ROWS = "20";
	private static final String TEXT_CSV_MIME = "text/csv";
	private static final String DL_PARAM_MSG = "Makes the response trigger a downloadable file rather than streaming the response";

	// TODO add content negotiation methods for all *data resources
	// TODO add lots more Swagger doco
	// TODO figure out how to get Swagger to support content negotiation with overloaded methods
	// TODO am I doing content negotiation correctly?
	// TODO do we accept LSID/species ID and/or a species name for the species related services?
	
	@Autowired
	@Qualifier("jenaRetrievalService")
	private RetrievalService retrievalService;
	
	@RequestMapping(path="/speciesData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data in JSON format", notes = "Gets Aekos data", tags="Data Retrieval")
    public SpeciesDataResponse speciesDataDotJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		// FIXME why do we never get more than 1 page when we use 0-20?
		SpeciesDataResponse result = retrievalService.getSpeciesDataJson(Arrays.asList(speciesNames), start, rows);
		resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
	}
	
	@RequestMapping(path="/speciesData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE,
    		headers="Accept="+MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get species data", notes = "Gets Aekos data", tags="Data Retrieval")
    public SpeciesDataResponse speciesDataJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		return speciesDataDotJson(speciesNames, start, rows, req, resp);
    }

	@RequestMapping(path="/speciesData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get species occurrence data in CSV format",
    		notes = "TODO", tags="Data Retrieval")
	@ApiResponses(@ApiResponse(code=200, message="Data is returned")) // FIXME how do we word this FIXME are there status code int constants somewhere?
    public void speciesDataDotCsv(
    		@RequestParam(name="speciesName") @ApiParam(value="Scientific name(s) of species to retrieve data for", example="Atriplex vesicaria") String[] speciesNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@RequestParam(required=false, defaultValue="false") @ApiParam(DL_PARAM_MSG) boolean download,
    		@ApiIgnore Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		resp.setContentType(TEXT_CSV_MIME);
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=aekosSpeciesData.csv"); // TODO give a more dynamic name
    	}
		RetrievalResponseHeader header = retrievalService.getSpeciesDataCsv(Arrays.asList(speciesNames), start, rows, responseWriter);
		resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), header));
    }
	
    @RequestMapping(path="/speciesData", method=RequestMethod.GET, produces=TEXT_CSV_MIME, headers="Accept="+TEXT_CSV_MIME)
    //FIXME what do I put in here? Do I copy from the other overloaded method?
    @ApiOperation(value = "Get species occurrence data",
			notes = "Gets species occurrence data in a Darwin Core compliant data format", tags="Data Retrieval")
    public void speciesDataCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@ApiIgnore Writer responseWriter, HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	boolean dontDownload = false;
		speciesDataDotCsv(speciesNames, start, rows, dontDownload, responseWriter, req, resp);
    }
    
    @RequestMapping(path="/traitData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get trait data in JSON format",
			notes = "Get trait data in a Darwin Core + traits format", tags="Data Retrieval")
    public TraitDataResponse traitDataDotJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	// TODO do we include units in the field name, as an extra value or as a header/metadata object in the resp
    	List<String> traits = traitNames != null ? Arrays.asList(traitNames) : Collections.emptyList();
    	// TODO validate start ! < 0
    	// TODO validate count > 0
    	TraitDataResponse result = retrievalService.getTraitDataJson(Arrays.asList(speciesNames), traits, start, rows);
    	resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
    }

    @RequestMapping(path="/traitData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get trait data", notes = "TODO", tags="Data Retrieval")
    public TraitDataResponse traitDataJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
		return traitDataDotJson(speciesNames, traitNames, start, rows, req, resp);
	}
    
    @RequestMapping(path="/traitData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get trait data in CSV format",
    		notes = "TODO", tags="Data Retrieval")
    public void traitDataDotCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		@RequestParam(required=false, defaultValue="false") @ApiParam(DL_PARAM_MSG) boolean download,
    		HttpServletRequest req, HttpServletResponse resp, @ApiIgnore Writer responseWriter) throws AekosApiRetrievalException {
    	resp.setContentType(TEXT_CSV_MIME);
    	if (download) {
    		resp.setHeader("Content-Disposition", "attachment;filename=aekosTraitData.csv"); // TODO give a more dynamic name
    	}
    	// TODO do we include units in the field name, as an extra value or as a header/metadata object in the resp
    	List<String> traits = traitNames != null ? Arrays.asList(traitNames) : Collections.emptyList();
    	// TODO validate start ! < 0
    	// TODO validate count > 0
    	String fullReqUrl = extractFullUrl(req);
    	// TODO figure out how to covert to CSV
    	RetrievalResponseHeader header = retrievalService.getTraitDataCsv(Arrays.asList(speciesNames), traits, start, rows, responseWriter);
    	// FIXME does it matter that we write the header after we write the body?
    	resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(fullReqUrl), header));
    }
    
    @RequestMapping(path="/traitData", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get trait data",
    		notes = "TODO", tags="Data Retrieval")
    public void traitDataCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="traitName", required=false) String[] traitNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp, @ApiIgnore Writer responseWriter) throws AekosApiRetrievalException {
    	traitDataDotCsv(speciesNames, traitNames, start, rows, false, req, resp, responseWriter);
    }
    
    @RequestMapping(path="/environmentData.json", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get environmental variable data in JSON format", notes = "TODO", tags="Data Retrieval")
    public EnvironmentDataResponse environmentDataDotJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="envVarName", required=false) String[] envVarNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	// TODO do we include units in the field name, as an extra value or as a header/metadata object in the resp
    	// TODO handle empty env vars
    	List<String> varNames = envVarNames != null ? Arrays.asList(envVarNames) : Collections.emptyList();
		EnvironmentDataResponse result = retrievalService.getEnvironmentalDataJson(Arrays.asList(speciesNames), varNames, start, rows);
		resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), RetrievalResponseHeader.newInstance(result)));
    	return result;
	}
    
    @RequestMapping(path="/environmentData", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get environmental variable data", notes = "TODO", tags="Data Retrieval")
    public EnvironmentDataResponse environmentDataJson(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="envVarName", required=false) String[] envVarNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp) throws AekosApiRetrievalException {
    	return environmentDataDotJson(speciesNames, envVarNames, start, rows, req, resp);
	}
    
    @RequestMapping(path="/environmentData.csv", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get environmental variable data in CSV format", notes = "TODO", tags="Data Retrieval")
    public void environmentDataDotCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="envVarName", required=false) String[] envVarNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp, @ApiIgnore Writer responseWriter) throws AekosApiRetrievalException {
    	// TODO do we include units in the field name, as an extra value or as a header/metadata object in the resp
    	resp.setContentType(TEXT_CSV_MIME);
    	List<String> varNames = envVarNames != null ? Arrays.asList(envVarNames) : Collections.emptyList();
    	RetrievalResponseHeader header = retrievalService.getEnvironmentalDataCsv(Arrays.asList(speciesNames), varNames, start, rows, responseWriter);
		// FIXME move header up?
		resp.addHeader(HttpHeaders.LINK, buildLinkHeader(UriComponentsBuilder.fromHttpUrl(extractFullUrl(req)), header));
	}
    
    @RequestMapping(path="/environmentData", method=RequestMethod.GET, produces=TEXT_CSV_MIME)
    @ApiOperation(value = "Get environmental variable data", notes = "TODO", tags="Data Retrieval")
    public void environmentDataCsv(
    		@RequestParam(name="speciesName") String[] speciesNames,
    		@RequestParam(name="envVarName", required=false) String[] envVarNames,
    		@RequestParam(required=false, defaultValue="0") @ApiParam("0-indexed result page start") int start,
    		@RequestParam(required=false, defaultValue=DEFAULT_ROWS) @ApiParam("result page size") int rows,
    		HttpServletRequest req, HttpServletResponse resp, @ApiIgnore Writer responseWriter) throws AekosApiRetrievalException {
    	environmentDataDotCsv(speciesNames, envVarNames, start, rows, req, resp, responseWriter);
	}
    
    private String buildLinkHeader(UriComponentsBuilder fromPath, RetrievalResponseHeader response) {
		// FIXME can't handle weird params like start=2, rows=3
    	int start = response.getStart();
		int rows = response.getRows();
		int pageNumber = response.getPageNumber();
		int totalPages = response.getTotalPages();
		StringBuilder result = new StringBuilder();
		boolean hasNextPage = pageNumber < totalPages;
		if (hasNextPage) {
			int startForNextPage = start + rows;
			String uriForNextPage = fromPath.replaceQueryParam("start", startForNextPage).build().toUriString();
			result.append(createLinkHeader(uriForNextPage, "next"));
		}
		boolean hasPrevPage = pageNumber > 1;
		if (hasPrevPage) {
			int startForPrevPage = start - rows;
			String uriForPrevPage = fromPath.replaceQueryParam("start", startForPrevPage).build().toUriString();
			appendCommaIfNecessary(result);
			result.append(createLinkHeader(uriForPrevPage, "prev"));
		}
		boolean hasFirstPage = pageNumber > 1;
		if (hasFirstPage) {
			String uriForFirstPage = fromPath.replaceQueryParam("start", 0).build().toUriString();
			appendCommaIfNecessary(result);
            result.append(createLinkHeader(uriForFirstPage, "first"));
		}
		boolean hasLastPage = pageNumber < totalPages;
		if (hasLastPage) {
			int startForLastPage = (totalPages-1) * rows;
			String uriForLastPage = fromPath.replaceQueryParam("start", startForLastPage).build().toUriString();
			appendCommaIfNecessary(result);
            result.append(createLinkHeader(uriForLastPage, "last"));
		}
		return result.toString();
	}
    
    void appendCommaIfNecessary(final StringBuilder linkHeader) {
        if (linkHeader.length() > 0) {
            linkHeader.append(", ");
        }
    }

    private String extractFullUrl(HttpServletRequest req) {
		return req.getRequestURL().toString() + "?" + req.getQueryString();
	}
    
    private static String createLinkHeader(final String uri, final String rel) {
        return "<" + uri + ">; rel=\"" + rel + "\"";
    }
    
    public static class RetrievalResponseHeader {
    	private final int start;
    	private final int rows;
    	private final int pageNumber;
    	private final int totalPages;
    	
		public RetrievalResponseHeader(int start, int rows, int pageNumber, int totalPages) {
			this.start = start;
			this.rows = rows;
			this.pageNumber = pageNumber;
			this.totalPages = totalPages;
		}

		public static RetrievalResponseHeader newInstance(AbstractResponse response) {
			ResponseHeader responseHeader = response.getResponseHeader();
			RetrievalResponseHeader result = new RetrievalResponseHeader(
					responseHeader.getParams().getStart(),
					responseHeader.getParams().getRows(),
					responseHeader.getPageNumber(),
					responseHeader.getTotalPages());
			return result;
		}

		public int getStart() {
			return start;
		}

		public int getRows() {
			return rows;
		}

		public int getPageNumber() {
			return pageNumber;
		}

		public int getTotalPages() {
			return totalPages;
		}
    }
}
