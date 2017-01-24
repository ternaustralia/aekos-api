package au.org.aekos.service.index;

import java.util.Collections;
import java.util.Set;

public class EnvironmentLoaderRecord {
	private final String locationID;
	private final Set<String> environmentalVariableNames;

	public EnvironmentLoaderRecord(String locationID, Set<String> environmentalVariableNames) {
		this.locationID = locationID;
		this.environmentalVariableNames = environmentalVariableNames;
	}

	public String getLocationID() {
		return locationID;
	}

	public Set<String> getEnvironmentalVariableNames() {
		return Collections.unmodifiableSet(environmentalVariableNames);
	}

	@Override
	public String toString() {
		return String.format("%s, %d env vars", locationID, environmentalVariableNames.size());
	}
}
