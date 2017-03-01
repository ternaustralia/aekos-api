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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pageNumber;
		result = prime * result + resultsPerPage;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageRequest other = (PageRequest) obj;
		if (pageNumber != other.pageNumber)
			return false;
		if (resultsPerPage != other.resultsPerPage)
			return false;
		return true;
	}
}
