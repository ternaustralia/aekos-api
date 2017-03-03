package au.org.aekos.api.model;

import java.util.List;

public class SpeciesDataResponseV1_1 extends AbstractResponse {

	private final List<SpeciesOccurrenceRecordV1_1> response;

	public SpeciesDataResponseV1_1(ResponseHeader responseHeader, List<SpeciesOccurrenceRecordV1_1> response) {
		super(responseHeader);
		this.response = response;
	}

	public List<SpeciesOccurrenceRecordV1_1> getResponse() {
		return response;
	}
}
