package au.org.aekos.api.producer.step.env.in;

public class InputEnvRecord {
	private final String locationID;
	private final double decimalLatitude;
	private final double decimalLongitude;
	private final String geodeticDatum;
	private final String eventDate;
	private final int month;
	private final int year;
	private final String locationName;
	private final String samplingProtocol;
	private final String rdfGraph;
	private final String rdfSubject;

	public InputEnvRecord(String locationID, double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationName,
			String samplingProtocol, String rdfSubject, String rdfGraph, String eventDate, int month, int year) {
		this.locationID = locationID;
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationName = locationName;
		this.samplingProtocol = samplingProtocol;
		this.rdfSubject = rdfSubject;
		this.rdfGraph = rdfGraph;
		this.eventDate = eventDate;
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

	public String getGeodeticDatum() {
		return geodeticDatum;
	}

	public String getLocationName() {
		return locationName;
	}

	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public String getRdfGraph() {
		return rdfGraph;
	}

	public String getRdfSubject() {
		return rdfSubject;
	}

	public String getEventDate() {
		return eventDate;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}
}
