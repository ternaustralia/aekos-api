package au.org.aekos.api.model;

import java.util.List;

public class EnvironmentDataResponse extends AbstractResponse {

	private final List<EnvironmentDataRecord> response;

	public EnvironmentDataResponse(ResponseHeader responseHeader, List<EnvironmentDataRecord> response) {
		super(responseHeader);
		this.response = response;
	}

	public List<EnvironmentDataRecord> getResponse() {
		return response;
	}
}
