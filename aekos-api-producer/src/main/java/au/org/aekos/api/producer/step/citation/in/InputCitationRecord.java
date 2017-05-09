package au.org.aekos.api.producer.step.citation.in;

import au.org.aekos.api.producer.Utils;

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
		return Utils.quote(samplingProtocol);
	}

	public String getBibliographicCitation() {
		return Utils.quote(bibliographicCitation);
	}

	public String getDatasetName() {
		return Utils.quote(datasetName);
	}

	public static String[] getCsvFields() {
		return new String[] {"samplingProtocol", "bibliographicCitation", "datasetName"};
	}
}
