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
	private final String locationId;
	private final String eventDate; // TODO consider giving toLongEventDate() method

	public SpeciesLoaderRecord(String speciesName, Set<Trait> traits, String samplingProtocol, String bibliographicCitation
			,String locationId, String eventDate) {
		this.speciesName = speciesName;
		this.traits = traits;
		this.samplingProtocol = samplingProtocol;
		this.bibliographicCitation = bibliographicCitation;
		this.locationId = locationId;
		this.eventDate = eventDate;
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

	public String getLocationId() {
		return locationId;
	}

	public String getEventDate() {
		return eventDate;
	}
	
	public String getJoinKey() {
		return locationId + eventDate;
	}

	@Override
	public String toString() {
		return String.format("%s %d traits", speciesName, traits.size());
	}
}
