package au.org.aekos.api.loader.service.load;

import java.util.Collections;
import java.util.Set;

public class EnvironmentLoaderRecord {
	private final String locationId;
	private final Set<String> environmentalVariableNames;
	private final String eventDate; // TODO consider giving toLongEventDate() method

	public EnvironmentLoaderRecord(String locationID, Set<String> environmentalVariableNames, String eventDate) {
		this.locationId = locationID;
		this.environmentalVariableNames = environmentalVariableNames;
		this.eventDate = eventDate;
	}

	public String getLocationId() {
		return locationId;
	}

	public Set<String> getEnvironmentalVariableNames() {
		return Collections.unmodifiableSet(environmentalVariableNames);
	}

	public String getEventDate() {
		return eventDate;
	}
	
	public String getJoinKey() {
		return locationId + eventDate;
	}

	@Override
	public String toString() {
		return String.format("%s, %d env vars", locationId, environmentalVariableNames.size());
	}
}
