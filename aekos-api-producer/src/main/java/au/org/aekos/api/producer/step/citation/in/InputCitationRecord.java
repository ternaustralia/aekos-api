package au.org.aekos.api.producer.step.citation.in;

import au.org.aekos.api.producer.CleanQuotedValue;

public class InputCitationRecord {
	private final String samplingProtocol;
	private final String bibliographicCitation;
	private final String datasetName;
	private final String licenceUrl;

	public InputCitationRecord(String samplingProtocol, String bibliographicCitation, String datasetName, String licenceUrl) {
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
		this.datasetName = datasetName;
		this.licenceUrl = licenceUrl;
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
	
	public String getLicenceUrl() {
		return new CleanQuotedValue(licenceUrl).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"samplingProtocol", "bibliographicCitation", "datasetName", "licenceUrl"};
	}
}
