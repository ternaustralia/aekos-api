package au.org.aekos.model;

public abstract class AbstractResponse {
	
	private final ResponseHeader responseHeader;
	
	public AbstractResponse(ResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	public ResponseHeader getResponseHeader() {
		return responseHeader;
	}
}
