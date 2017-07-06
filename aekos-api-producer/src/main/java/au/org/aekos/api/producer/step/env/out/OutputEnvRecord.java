package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.CleanQuotedValue;
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

	public String getGeodeticDatum() {
		return new CleanQuotedValue(coreRecord.getGeodeticDatum()).getValue();
	}

	public String getLocationID() {
		return new CleanQuotedValue(coreRecord.getLocationID()).getValue();
	}

	public String getLocationName() {
		return new CleanQuotedValue(coreRecord.getLocationName()).getValue();
	}

	public String getEventDate() {
		return new CleanQuotedValue(coreRecord.getEventDate()).getValue();
	}
	
	public int getMonth() {
		return coreRecord.getMonth();
	}

	public int getYear() {
		return coreRecord.getYear();
	}
	
	public String getSamplingProtocol() {
		return new CleanQuotedValue(coreRecord.getSamplingProtocol()).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"locationID", "eventDate", "month", "year", "decimalLatitude", "decimalLongitude",
				"geodeticDatum", "locationName", "samplingProtocol"};
	}
}
