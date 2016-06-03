package au.org.aekos.model;

public class SpeciesOccurrenceRecord {

	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String locationID;
    private final String scientificName;
    private final String eventDate;
    private final String year;
    private final String month;
    private final String bibliographicCitation;
    private final String datasetID;

	public SpeciesOccurrenceRecord(double decimalLatitude, double decimalLongitude, String locationID,
			String scientificName, String eventDate, String year, String month, String bibliographicCitation,
			String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.locationID = locationID;
		this.scientificName = scientificName;
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

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public static SpeciesOccurrenceRecord deserialiseFrom(String[] fields) {
    	double decimalLatitudeField = Double.parseDouble(fields[0]);
		double decimalLongitudeField = Double.parseDouble(fields[1]);
		String locationIdField = fields[2];
		String scientificNameField = fields[3];
		String eventDateField = fields[4];
		String yearField = fields[5];
		String monthField = fields[6];
		String bibliographicCitationField = fields[7];
		String datasetIdField = fields[8];
		SpeciesOccurrenceRecord result = new SpeciesOccurrenceRecord(decimalLatitudeField, decimalLongitudeField, 
				locationIdField, scientificNameField, eventDateField, yearField, 
				monthField, bibliographicCitationField, datasetIdField);
		return result;
    }
}
