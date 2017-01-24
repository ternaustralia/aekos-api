package au.org.aekos.service.index;

import java.util.Collections;
import java.util.Set;

public class IndexLoaderRecord {
	// Could be scientificName or taxonRemarks
	private final String speciesName;
	private final Set<String> traitNames;
	private final Set<String> environmentalVariableNames;

	public IndexLoaderRecord(String speciesName, Set<String> traitNames, Set<String> environmentalVariableNames) {
		this.speciesName = speciesName;
		this.traitNames = traitNames;
		this.environmentalVariableNames = environmentalVariableNames;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public Set<String> getTraitNames() {
		return Collections.unmodifiableSet(traitNames);
	}

	public Set<String> getEnvironmentalVariableNames() {
		return Collections.unmodifiableSet(environmentalVariableNames);
	}

	@Override
	public String toString() {
		return speciesName + " " + traitNames.size() + " traits, " + environmentalVariableNames.size() + " env vars";
	}
}
