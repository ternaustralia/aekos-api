package au.org.aekos.api.loader.service.index;

import java.util.Collections;
import java.util.Set;

public class SpeciesLoaderRecord {
	/** Could be scientificName or taxonRemarks */
	private final String speciesName;
	private final String samplingProtocol;
	private final Set<String> traitNames;
	private final String bibliographicCitation;

	public SpeciesLoaderRecord(String speciesName, Set<String> traitNames, String samplingProtocol, String bibliographicCitation) {
		this.speciesName = speciesName;
		this.traitNames = traitNames;
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public Set<String> getTraitNames() {
		return Collections.unmodifiableSet(traitNames);
	}
	
	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	@Override
	public String toString() {
		return String.format("%s %d traits", speciesName, traitNames.size());
	}
}
