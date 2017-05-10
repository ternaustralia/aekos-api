package au.org.aekos.api.producer.step.env.in;

public class InputEnvRecord {
	private final String locationID;
	private final double decimalLatitude;
	private final double decimalLongitude;
	private final String eventDate;
	private final String geodeticDatum;
	private final String locationName;
	private final int month;
	private final int year;

	public InputEnvRecord(String locationID, double decimalLatitude, double decimalLongitude, String eventDate, String geodeticDatum, String locationName,
			int month, int year) {
		this.locationID = locationID;
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.eventDate = eventDate;
		this.geodeticDatum = geodeticDatum;
		this.locationName = locationName;
		this.month = month;
		this.year = year;
	}

	public String getLocationID() {
		return locationID;
	}

	public double getDecimalLatitude() {
		return decimalLatitude;
	}

	public double getDecimalLongitude() {
		return decimalLongitude;
	}

	public String getEventDate() {
		return eventDate;
	}

	public String getGeodeticDatum() {
		return geodeticDatum;
	}

	public String getLocationName() {
		return locationName;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}
}
