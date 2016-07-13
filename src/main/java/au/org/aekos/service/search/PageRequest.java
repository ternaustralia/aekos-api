package au.org.aekos.service.search;

public class PageRequest {
	
	public int pageNumber;
	public int resultsPerPage;
	
	public PageRequest(int pageNumber, int resultsPerPage) {
		super();
		this.pageNumber = pageNumber;
		this.resultsPerPage = resultsPerPage;
	} 
	

}
