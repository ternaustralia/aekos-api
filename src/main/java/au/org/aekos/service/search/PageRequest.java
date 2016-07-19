package au.org.aekos.service.search;


/**
 * PageRequest pagination isn't zero indexed!!
 * Page numbers start from 1
 * 
 * if results per page <= 0  use the default results per page
 * 
 * @author Ben
 */
public class PageRequest {
	
	public int pageNumber;
	public int resultsPerPage;
	
	//TODO Dubious on hiding this here, perhaps rename this object PaginationContext or something
	//Left it so the interface is uncluttered tonight . .  not sure what to do with the results yet
	public PageResultMetadata resultMetadata = null;
	
	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getResultsPerPage() {
		return resultsPerPage;
	}

	public void setResultsPerPage(int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	public PageResultMetadata getResultMetadata() {
		return resultMetadata;
	}

	public void setResultMetadata(PageResultMetadata resultMetadata) {
		this.resultMetadata = resultMetadata;
	}

	public PageRequest(int pageNumber, int resultsPerPage) {
		super();
		this.pageNumber = pageNumber;
		this.resultsPerPage = resultsPerPage;
	} 
	

}
