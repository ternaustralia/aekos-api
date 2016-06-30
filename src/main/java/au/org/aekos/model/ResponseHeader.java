package au.org.aekos.model;

import java.util.Date;

public class ResponseHeader {
	private final int numFound;
	private final int pageNumber;
	private final int totalPages;
	private final int elapsedTime;
	private final AbstractParams params;
	
	public ResponseHeader(int numFound, int pageNumber, int totalPages, int elapsedTime, AbstractParams params) {
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
	public AbstractParams getParams() {
		return params;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public int getTotalPages() {
		return totalPages;
	}

	public static ResponseHeader newInstance(int start, int rows, int numFoundParam, long startTime, AbstractParams paramsParam) {
    	int elapsedTime = (int) (new Date().getTime() - startTime);
		int totalPages = calculateTotalPages(rows, numFoundParam);
		int pageNumber = calculatePageNumber(start, numFoundParam, totalPages);
		return new ResponseHeader(numFoundParam, pageNumber, totalPages, elapsedTime, paramsParam);
	}
	
	static int calculateTotalPages(int rows, int numFound) {
		return (int)Math.ceil((double)numFound / (double)rows);
	}

	static int calculatePageNumber(int start, int numFound, int totalPages) {
		if (numFound == 0) {
			return 0;
		}
		if (start == 0) {
			return 1;
		}
		double decimalProgress = ((double)start+1) / (double)numFound;
		double decimalPageNumber = decimalProgress * (double)totalPages;
		return (int)Math.ceil(decimalPageNumber);
	}
}