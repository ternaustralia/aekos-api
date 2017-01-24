package au.org.aekos.service.index;

import java.util.Collections;
import java.util.Set;

public class SpeciesLoaderRecord {
	// Could be scientificName or taxonRemarks
	private final String speciesName;
	private final Set<String> traitNames;

	public SpeciesLoaderRecord(String speciesName, Set<String> traitNames) {
		this.speciesName = speciesName;
		this.traitNames = traitNames;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public Set<String> getTraitNames() {
		return Collections.unmodifiableSet(traitNames);
	}

	@Override
	public String toString() {
		return String.format("%s %d traits", speciesName, traitNames.size());
	}
}
