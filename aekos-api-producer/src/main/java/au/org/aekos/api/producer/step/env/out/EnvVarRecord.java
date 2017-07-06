package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.CleanQuotedValue;

/**
 * Attribute could be a Trait or an Environmental Variable
 */
public class EnvVarRecord {
	private final String locationID;
	private final String eventDate;
	private final String name;
	private final String value;
	private final String units;
	
	public EnvVarRecord(String locationID, String eventDate, String name, String value, String units) {
		this.locationID = locationID;
		this.eventDate = eventDate;
		this.name = name;
		this.value = value;
		this.units = units;
	}

	public String getLocationID() {
		return new CleanQuotedValue(locationID).getValue();
	}
	
	public String getEventDate() {
		return new CleanQuotedValue(eventDate).getValue();
	}

	public String getName() {
		return new CleanQuotedValue(name).getValue();
	}

	public String getValue() {
		return new CleanQuotedValue(value).getValue();
	}

	public String getUnits() {
		return new CleanQuotedValue(units).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"locationID", "eventDate", "name", "value", "units"};
	}
}
