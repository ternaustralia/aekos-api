package au.org.aekos.api.producer.step.citation.in;

import au.org.aekos.api.producer.CleanQuotedValue;

public class InputCitationRecord {
	private final String samplingProtocol;
	private final String bibliographicCitation;
	private final String datasetName;

	public InputCitationRecord(String samplingProtocol, String bibliographicCitation, String datasetName) {
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
		this.datasetName = datasetName;
	}

	public String getSamplingProtocol() {
		return new CleanQuotedValue(samplingProtocol).getValue();
	}

	public String getBibliographicCitation() {
		return new CleanQuotedValue(bibliographicCitation).getValue();
	}

	public String getDatasetName() {
		return new CleanQuotedValue(datasetName).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"samplingProtocol", "bibliographicCitation", "datasetName"};
	}
}
