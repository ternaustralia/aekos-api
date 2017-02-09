package au.org.aekos.api.model;

import java.util.List;

public class SpeciesDataResponse extends AbstractResponse {

	private final List<SpeciesOccurrenceRecord> response;

	public SpeciesDataResponse(ResponseHeader responseHeader, List<SpeciesOccurrenceRecord> response) {
		super(responseHeader);
		this.response = response;
	}

	public List<SpeciesOccurrenceRecord> getResponse() {
		return response;
	}
}
