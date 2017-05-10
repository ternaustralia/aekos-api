package au.org.aekos.api.producer.step.species.in;

public class InputSpeciesRecord {
	private final String id;
	private final String datasetName;
	private final double decimalLatitude;
	private final double decimalLongitude;
	private final String eventDate;
	private final String geodeticDatum;
	private final int individualCount;
	private final String locationID;
	private final String locationName;
	private final int month;
	private final String samplingProtocol;
	private final String scientificName;
	private final String taxonRemarks;
	private final int year;

	public InputSpeciesRecord(String id, String datasetName, double decimalLatitude, double decimalLongitude, String eventDate, String geodeticDatum,
			int individualCount, String locationID, String locationName, int month, String samplingProtocol, String scientificName, String taxonRemarks, int year) {
		this.id = id;
		this.datasetName = datasetName;
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.eventDate = eventDate;
		this.geodeticDatum = geodeticDatum;
		this.individualCount = individualCount;
		this.locationID = locationID;
		this.locationName = locationName;
		this.month = month;
		this.samplingProtocol = samplingProtocol;
		this.scientificName = scientificName;
		this.taxonRemarks = taxonRemarks;
		this.year = year;
		if (scientificName == null && taxonRemarks == null) {
			String template = "Data problem: record with id '%s' is missing BOTH scientificName and taxonRemarks";
			throw new IllegalStateException(String.format(template, id));
		}
	}

	public String getId() {
		return id;
	}

	public String getDatasetName() {
		return datasetName;
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

	public int getIndividualCount() {
		return individualCount;
	}

	public String getLocationID() {
		return locationID;
	}

	public String getLocationName() {
		return locationName;
	}

	public int getMonth() {
		return month;
	}

	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getTaxonRemarks() {
		return taxonRemarks;
	}

	public int getYear() {
		return year;
	}
}
