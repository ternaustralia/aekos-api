package au.org.aekos.api.service.search;

public class PageResultMetadata {
	
	public int totalResults;
	public int pageNumber;
	
	public PageResultMetadata(){}
	
	public PageResultMetadata(int totalResults, int pageNumber) {
		super();
		this.totalResults = totalResults;
		this.pageNumber = pageNumber;
	}

}
