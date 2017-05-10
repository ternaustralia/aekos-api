package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.Utils;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class OutputEnvRecord {
	private final InputEnvRecord coreRecord;

	public OutputEnvRecord(InputEnvRecord coreRecord) {
		this.coreRecord = coreRecord;
	}

	public double getDecimalLatitude() {
		return coreRecord.getDecimalLatitude();
	}

	public double getDecimalLongitude() {
		return coreRecord.getDecimalLongitude();
	}

	public String getEventDate() {
		return Utils.quote(coreRecord.getEventDate());
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

	public int getMonth() {
		return coreRecord.getMonth();
	}

	public int getYear() {
		return coreRecord.getYear();
	}

	public static String[] getCsvFields() {
		return new String[] {"locationID", "decimalLatitude", "decimalLongitude", "eventDate", "geodeticDatum",
				"locationName", "month", "year"};
	}
}
