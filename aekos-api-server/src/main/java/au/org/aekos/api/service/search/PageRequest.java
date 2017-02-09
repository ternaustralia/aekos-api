package au.org.aekos.api.service.search;


/**
 * PageRequest pagination isn't zero indexed!!
 * Page numbers start from 1
 * 
 * if results per page <= 0  use the default results per page
 * 
 * @author Ben
 */
public class PageRequest {
	
	private final int pageNumber;
	private final int resultsPerPage;
	
	//TODO Dubious on hiding this here, perhaps rename this object PaginationContext or something
	//Left it so the interface is uncluttered tonight . .  not sure what to do with the results yet
	public PageResultMetadata resultMetadata = null;
	
	public PageRequest(int pageNumber, int resultsPerPage) {
		this.pageNumber = pageNumber;
		this.resultsPerPage = resultsPerPage;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}

	public int getResultsPerPage() {
		return resultsPerPage;
	}

	public PageResultMetadata getResultMetadata() {
		return resultMetadata;
	}

	public void setResultMetadata(PageResultMetadata resultMetadata) {
		this.resultMetadata = resultMetadata;
	}
}
