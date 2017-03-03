package au.org.aekos.api.model;

import java.util.List;

public class SpeciesDataResponseV1_0 extends AbstractResponse {

	private final List<SpeciesOccurrenceRecordV1_0> response;

	public SpeciesDataResponseV1_0(ResponseHeader responseHeader, List<SpeciesOccurrenceRecordV1_0> response) {
		super(responseHeader);
		this.response = response;
	}

	public List<SpeciesOccurrenceRecordV1_0> getResponse() {
		return response;
	}
}
