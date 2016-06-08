package au.org.aekos.model;

public class SpeciesOccurrenceRecord {

	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String geodeticDatum;
    private final String locationID;
    private final String scientificName;
    private final int individualCount;
    private final String eventDate;
    private final int year;
    private final int month;
    private final String bibliographicCitation;
    private final String datasetID;

	public SpeciesOccurrenceRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month, String bibliographicCitation,
			String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
		this.scientificName = scientificName;
		this.individualCount = individualCount;
		this.eventDate = eventDate;
		this.year = year;
		this.month = month;
		this.bibliographicCitation = bibliographicCitation;
		this.datasetID = datasetID;
	}

	public double getDecimalLatitude() {
		return decimalLatitude;
	}

	public double getDecimalLongitude() {
		return decimalLongitude;
	}

	public String getLocationID() {
		return locationID;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getEventDate() {
		return eventDate;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public String getGeodeticDatum() {
		return geodeticDatum;
	}

	public int getIndividualCount() {
		return individualCount;
	}

	public static SpeciesOccurrenceRecord deserialiseFrom(String[] fields) {
    	double decimalLatitudeField = Double.parseDouble(fields[0]);
		double decimalLongitudeField = Double.parseDouble(fields[1]);
		String geodeticDatumField = fields[2];
		String locationIdField = fields[3];
		String scientificNameField = fields[4];
		int individualCountField = Integer.parseInt(fields[5]);
		String eventDateField = fields[6];
		int yearField = Integer.parseInt(fields[7]);
		int monthField = Integer.parseInt(fields[8]);
		String bibliographicCitationField = fields[9];
		String datasetIdField = fields[10];
		SpeciesOccurrenceRecord result = new SpeciesOccurrenceRecord(decimalLatitudeField, decimalLongitudeField,
				geodeticDatumField, locationIdField, scientificNameField, individualCountField, eventDateField,
				yearField, monthField, bibliographicCitationField, datasetIdField);
		return result;
    }
}
