package au.org.aekos.api.service.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import au.org.aekos.api.model.VisitInfo;

class VisitTracker {

	private Map<VisitKey, VisitInfo> visitInfos = new HashMap<>();
	
	public VisitInfo getVisitInfoFor(String locationID, String eventDate) {
		return visitInfos.get(new VisitKey(locationID, eventDate));
	}

	public void addVisitInfo(String locationID, String eventDate, VisitInfo item) {
		visitInfos.put(new VisitKey(locationID, eventDate), item);
	}

	public int visitSize() {
		return visitInfos.size();
	}

	public String getLocationIDAndEventDateSparqlParamList() {
		return visitInfos.keySet().stream()
				.map(e -> String.format("%s\" \"%s", e.getLocationId(), e.getEventDate()))
				.distinct()
				.collect(Collectors.joining("\") (\"", "(\"", "\")"));
	}

	public boolean isEmpty() {
		return visitInfos.isEmpty();
	}
}

class VisitKey {
	private final String locationId;
	private final String eventDate;
	
	public VisitKey(String locationId, String eventDate) {
		this.locationId = locationId;
		this.eventDate = eventDate;
	}

	public String getLocationId() {
		return locationId;
	}

	public String getEventDate() {
		return eventDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
		result = prime * result + ((locationId == null) ? 0 : locationId.hashCode());
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
		VisitKey other = (VisitKey) obj;
		if (eventDate == null) {
			if (other.eventDate != null)
				return false;
		} else if (!eventDate.equals(other.eventDate))
			return false;
		if (locationId == null) {
			if (other.locationId != null)
				return false;
		} else if (!locationId.equals(other.locationId))
			return false;
		return true;
	}
}
