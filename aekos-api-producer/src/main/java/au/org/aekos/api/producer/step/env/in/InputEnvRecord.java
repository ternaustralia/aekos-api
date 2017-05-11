package au.org.aekos.api.producer.step.env.in;

public class InputEnvRecord {
	private final String locationID;
	private final double decimalLatitude;
	private final double decimalLongitude;
	private final String geodeticDatum;
	private final String locationName;
	private final String samplingProtocol;

	public InputEnvRecord(String locationID, double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationName,
			String samplingProtocol) {
		this.locationID = locationID;
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationName = locationName;
		this.samplingProtocol = samplingProtocol;
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
}
