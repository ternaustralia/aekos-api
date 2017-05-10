package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.Utils;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class OutputEnvRecord {
	private final InputEnvRecord coreRecord;
	private final String eventDate;
	private final int month;
	private final int year;

	public OutputEnvRecord(InputEnvRecord coreRecord, String eventDate, int month, int year) {
		this.coreRecord = coreRecord;
		this.eventDate = eventDate;
		this.month = month;
		this.year = year;
	}

	public double getDecimalLatitude() {
		return coreRecord.getDecimalLatitude();
	}

	public double getDecimalLongitude() {
		return coreRecord.getDecimalLongitude();
	}

	public String getGeodeticDatum() {
		return Utils.quote(coreRecord.getGeodeticDatum());
	}

	public String getLocationID() {
		return Utils.quote(coreRecord.getLocationID());
	}

	public String getLocationName() {
		return Utils.quote(coreRecord.getLocationName());
	}

	public String getEventDate() {
		return Utils.quote(eventDate);
	}
	
	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public static String[] getCsvFields() {
		return new String[] {"locationID", "decimalLatitude", "decimalLongitude", "eventDate", "geodeticDatum",
				"locationName", "month", "year"};
	}
}
