package au.org.aekos.service.index;

public class CitationLoaderRecord {
	private final String samplingProtocol;
	private final String bibliographicCitation;

	public CitationLoaderRecord(String samplingProtocol, String bibliographicCitation) {
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}
	
	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	@Override
	public String toString() {
		return "citation for " + samplingProtocol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bibliographicCitation == null) ? 0 : bibliographicCitation.hashCode());
		result = prime * result + ((samplingProtocol == null) ? 0 : samplingProtocol.hashCode());
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
		CitationLoaderRecord other = (CitationLoaderRecord) obj;
		if (bibliographicCitation == null) {
			if (other.bibliographicCitation != null)
				return false;
		} else if (!bibliographicCitation.equals(other.bibliographicCitation))
			return false;
		if (samplingProtocol == null) {
			if (other.samplingProtocol != null)
				return false;
		} else if (!samplingProtocol.equals(other.samplingProtocol))
			return false;
		return true;
	}
}
