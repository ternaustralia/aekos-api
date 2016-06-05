package au.org.aekos.model;

import java.util.List;

public class TraitDataResponse {

	private final ResponseHeader responseHeader;
	private final List<TraitDataRecord> response;

	public TraitDataResponse(ResponseHeader responseHeader, List<TraitDataRecord> response) {
		this.responseHeader = responseHeader;
		this.response = response;
	}

	public ResponseHeader getResponseHeader() {
		return responseHeader;
	}

	public List<TraitDataRecord> getResponse() {
		return response;
	}

	public static class ResponseHeader {
		private final int numFound;
		private final int pageNumber;
		private final int totalPages;
		private final int elapsedTime;
		private final Params params;
		
		public ResponseHeader(int numFound, int pageNumber, int totalPages, int elapsedTime, Params params) {
			this.numFound = numFound;
			this.pageNumber = pageNumber;
			this.totalPages = totalPages;
			this.elapsedTime = elapsedTime;
			this.params = params;
		}
		
		public int getNumFound() {
			return numFound;
		}
		public int getElapsedTime() {
			return elapsedTime;
		}
		public Params getParams() {
			return params;
		}
		public int getPageNumber() {
			return pageNumber;
		}
		public int getTotalPages() {
			return totalPages;
		}
	}
	
	public static class Params {
		private final int start;
		private final int rows;
		private final List<String> speciesNames;
		private final List<String> traitNames;

		public Params(int start, int rows, List<String> speciesNames, List<String> traitNames) {
			this.start = start;
			this.rows = rows;
			this.speciesNames = speciesNames;
			this.traitNames = traitNames;
		}

		public int getStart() {
			return start;
		}

		public int getRows() {
			return rows;
		}

		public List<String> getSpeciesNames() {
			return speciesNames;
		}

		public List<String> getTraitNames() {
			return traitNames;
		}
	}

	public static TraitDataResponse newInstance(List<TraitDataRecord> recordsPage, int startParam, int countParam, int numFoundParam,
			List<String> speciesNamesParam, List<String> traitNamesParam, int elapsedTimeParam, int pageNumberParam, int totalPagesParam) {
		Params params = new Params(startParam, countParam, speciesNamesParam, traitNamesParam);
		ResponseHeader headerParam = new ResponseHeader(numFoundParam, pageNumberParam, totalPagesParam, elapsedTimeParam, params);
		return new TraitDataResponse(headerParam, recordsPage);
	}
}
