package au.org.aekos.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocationInfo {
	private final String samplingProtocol;
	private final String bibliographicCitation;
	private final Set<String> scientificNames = new HashSet<>();
	private final Set<String> taxonRemarks = new HashSet<>();
	
	public LocationInfo(String samplingProtocol, String bibliographicCitation) {
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
	}
	
	public String getSamplingProtocol() {
		return samplingProtocol;
	}
	
	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public Set<String> getScientificNames() {
		return Collections.unmodifiableSet(scientificNames);
	}

	public void addScientificName(String scientificName) {
		scientificNames.add(scientificName);
	}

	public Set<String> getTaxonRemarks() {
		return Collections.unmodifiableSet(taxonRemarks);
	}
	
	public void addTaxonRemarks(String taxonRemark) {
		taxonRemarks.add(taxonRemark);
	}
}