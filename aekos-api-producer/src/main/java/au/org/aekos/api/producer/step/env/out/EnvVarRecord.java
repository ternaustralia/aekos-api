package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.Utils;

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
		return Utils.quote(locationID);
	}
	
	public String getEventDate() {
		return Utils.quote(eventDate);
	}

	public String getName() {
		return Utils.quote(name);
	}

	public String getValue() {
		return Utils.quote(value);
	}

	public String getUnits() {
		return Utils.quote(units);
	}

	public static String[] getCsvFields() {
		return new String[] {"locationID", "eventDate", "name", "value", "units"};
	}
}
