package au.org.aekos.model;

import java.util.List;

public class TraitDataResponse extends AbstractResponse {

	private final List<TraitDataRecord> response;

	public TraitDataResponse(ResponseHeader responseHeader, List<TraitDataRecord> response) {
		super(responseHeader);
		this.response = response;
	}

	public List<TraitDataRecord> getResponse() {
		return response;
	}
}
