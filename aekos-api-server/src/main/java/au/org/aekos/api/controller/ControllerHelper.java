package au.org.aekos.api.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriComponentsBuilder;

class ControllerHelper {

	static final String RETRIEVAL_BY_SPECIES_DESC = "Retrieve data using parameters from search";
	static final String RETRIEVAL_ALL_DESC = "Retrieve all records";
	static final String CONTENT_NEGOTIATION_FRAGMENT = " using content negotation to determine the response type.";
	static final String DEFAULT_START = "0";
	static final String DEFAULT_ROWS = "20";
	static final String TEXT_CSV_MIME = "text/csv";
	static final String DL_PARAM_MSG = "Makes the response trigger a downloadable file rather than streaming the response";
	static final String DATA_RETRIEVAL_ALL_TAG = "Data Retrieval (everything)";
	static final String DATA_RETRIEVAL_BY_SPECIES_TAG = "Data Retrieval by Species";
	static final String DATA_RETRIEVAL_BY_SPECIES_ARCHIVED_TAG = "Data Retrieval by Species - Archived";
	private static SimpleDateFormat SDF = new SimpleDateFormat("YYYYMMdd");
	
	private ControllerHelper() {}
	
	// TODO do we accept LSID/species ID and/or a species name for the species related services?
	
    static String buildHateoasLinkHeader(UriComponentsBuilder fromPath, RetrievalResponseHeader response) {
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
    
    static String extractFullUrl(HttpServletRequest req) {
    	return req.getRequestURL().toString() + "?" + req.getQueryString();
    }
    
    static void appendCommaIfNecessary(final StringBuilder linkHeader) {
        if (linkHeader.length() > 0) {
            linkHeader.append(", ");
        }
    }
    
    private static String createLinkHeader(final String uri, final String rel) {
        return "<" + uri + ">; rel=\"" + rel + "\"";
    }

	public static String generateDownloadFileName(String dataTypeName, Date when) {
		String dateFragment = SDF.format(when);
		return "aekos-api-" + dataTypeName + "-data-" + dateFragment + ".csv";
	}
}
