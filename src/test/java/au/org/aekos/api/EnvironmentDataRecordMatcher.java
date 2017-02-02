package au.org.aekos.api;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import au.org.aekos.api.model.EnvironmentDataRecord;

public class EnvironmentDataRecordMatcher extends BaseMatcher<EnvironmentDataRecord> {
	private final String eventDate;
	private final String locationID;
	
	public EnvironmentDataRecordMatcher(String eventDate, String locationID) {
		this.eventDate = eventDate;
		this.locationID = locationID;
	}

	@Override
	public boolean matches(Object item) {
		if (item == null) {
			return false;
		}
		EnvironmentDataRecord castItem = (EnvironmentDataRecord) item;
		if (eventDate.equals(castItem.getEventDate()) && locationID.equals(castItem.getLocationID())) {
			return true;
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("env record with eventDate='%s' locationID='%s'", eventDate, locationID));
	}
	
	public static Matcher<EnvironmentDataRecord> isEnvRecord(String eventDate, String locationID) {
		return new EnvironmentDataRecordMatcher(eventDate, locationID);
	}
}