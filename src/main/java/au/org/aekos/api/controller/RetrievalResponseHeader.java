package au.org.aekos.api.controller;

import au.org.aekos.api.model.AbstractResponse;
import au.org.aekos.api.model.ResponseHeader;

public class RetrievalResponseHeader {
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