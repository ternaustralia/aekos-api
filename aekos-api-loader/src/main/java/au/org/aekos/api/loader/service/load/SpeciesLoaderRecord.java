package au.org.aekos.api.loader.service.load;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import au.org.aekos.api.loader.service.index.Trait;

public class SpeciesLoaderRecord {
	/** Could be scientificName or taxonRemarks */
	private final String speciesName;
	private final String samplingProtocol;
	private final Set<Trait> traits;
	private final String bibliographicCitation;

	public SpeciesLoaderRecord(String speciesName, Set<Trait> traits, String samplingProtocol, String bibliographicCitation) {
		this.speciesName = speciesName;
		this.traits = traits;
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public Set<Trait> getTraits() {
		return Collections.unmodifiableSet(traits);
	}

	public Set<String> getTraitNames() {
		Set<String> result = traits.stream()
				.map(e -> e.getName())
				.collect(Collectors.toSet());
		return Collections.unmodifiableSet(result);
	}
	
	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	@Override
	public String toString() {
		return String.format("%s %d traits", speciesName, traits.size());
	}
}
