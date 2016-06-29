package au.org.aekos.model;

public class SpeciesOccurrenceRecord {

	private static final String CSV_SEPARATOR = ",";
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
    private final String samplingProtocol;

	public SpeciesOccurrenceRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month, String bibliographicCitation,
			String samplingProtocol) {
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
		this.samplingProtocol = samplingProtocol;
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

	public String getSamplingProtocol() {
		return samplingProtocol;
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
		String samplingProtocolField = fields[10];
		SpeciesOccurrenceRecord result = new SpeciesOccurrenceRecord(decimalLatitudeField, decimalLongitudeField,
				geodeticDatumField, locationIdField, scientificNameField, individualCountField, eventDateField,
				yearField, monthField, bibliographicCitationField, samplingProtocolField);
		return result;
    }

	public String toCsv() {
		StringBuilder result = new StringBuilder();
		result.append(decimalLatitude);
		result.append(CSV_SEPARATOR);
		result.append(decimalLongitude);
		result.append(CSV_SEPARATOR);
		result.append(quote(geodeticDatum));
		result.append(CSV_SEPARATOR);
		result.append(quote(locationID));
		result.append(CSV_SEPARATOR);
		result.append(quote(scientificName));
		result.append(CSV_SEPARATOR);
		result.append(individualCount);
		result.append(CSV_SEPARATOR);
		result.append(quote(eventDate));
		result.append(CSV_SEPARATOR);
		result.append(year);
		result.append(CSV_SEPARATOR);
		result.append(month);
		result.append(CSV_SEPARATOR);
		result.append(quote(bibliographicCitation));
		result.append(CSV_SEPARATOR);
		result.append(quote(samplingProtocol));
		return result.toString();
	}

	public static String getCsvHeader() {
		StringBuilder result = new StringBuilder();
		result.append(quote("decimalLatitude"));
		result.append(CSV_SEPARATOR);
		result.append(quote("decimalLongitude"));
		result.append(CSV_SEPARATOR);
		result.append(quote("geodeticDatum"));
		result.append(CSV_SEPARATOR);
		result.append(quote("locationID"));
		result.append(CSV_SEPARATOR);
		result.append(quote("scientificName"));
		result.append(CSV_SEPARATOR);
		result.append(quote("individualCount"));
		result.append(CSV_SEPARATOR);
		result.append(quote("eventDate"));
		result.append(CSV_SEPARATOR);
		result.append(quote("year"));
		result.append(CSV_SEPARATOR);
		result.append(quote("month"));
		result.append(CSV_SEPARATOR);
		result.append(quote("bibliographicCitation"));
		result.append(CSV_SEPARATOR);
		result.append(quote("samplingProtocol"));
		return result.toString();
	}
	
	private static String quote(String value) {
		return "\"" + value + "\"";
	}
}
