package au.org.aekos.service.retrieval;

import java.util.Collections;
import java.util.Set;

public class IndexLoaderRecord {
	private final String scientificName;
	private final Set<String> traitNames;
	private final Set<String> environmentalVariableNames;

	public IndexLoaderRecord(String scientificName, Set<String> traitNames, Set<String> environmentalVariableNames) {
		this.scientificName = scientificName;
		this.traitNames = traitNames;
		this.environmentalVariableNames = environmentalVariableNames;
	}

	public String getScientificName() {
		return scientificName;
	}

	public Set<String> getTraitNames() {
		return Collections.unmodifiableSet(traitNames);
	}

	public Set<String> getEnvironmentalVariableNames() {
		return Collections.unmodifiableSet(environmentalVariableNames);
	}
}
