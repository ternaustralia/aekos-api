package au.org.aekos.api.model;

public class SpeciesOccurrenceRecord {

	static final String CSV_SEPARATOR = ",";
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
	private final String taxonRemarks;

	/**
	 * Construct a record with a scientificName
	 */
	public SpeciesOccurrenceRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month, String bibliographicCitation,
			String samplingProtocol) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
		this.scientificName = scientificName;
		this.taxonRemarks = null;
		this.individualCount = individualCount;
		this.eventDate = eventDate;
		this.year = year;
		this.month = month;
		this.bibliographicCitation = bibliographicCitation;
		this.samplingProtocol = samplingProtocol;
	}
	
	/**
	 * Construct a record with taxonRemarks
	 */
	public SpeciesOccurrenceRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			int individualCount, String eventDate, int year, int month, String bibliographicCitation, String samplingProtocol,
			String taxonRemarks) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
		this.scientificName = null;
		this.taxonRemarks = taxonRemarks;
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

	public String getTaxonRemarks() {
		return taxonRemarks;
	}
	
	/**
	 * Appends the scientificName or taxonRemarks as appropriate to the supplied item.
	 */
	public void appendSpeciesNameTo(VisitInfo item) {
		if (isScientificNameSupplied()) {
			item.addScientificName(scientificName);
			return;
		}
		item.addTaxonRemarks(taxonRemarks);
	}
	
	private boolean isScientificNameSupplied() {
		return scientificName != null;
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
		result.append(isScientificNameSupplied() ? quote(scientificName) : quote(""));
		result.append(CSV_SEPARATOR);
		result.append(isScientificNameSupplied() ? quote("") : quote(taxonRemarks));
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
		result.append(quote("taxonRemarks"));
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
	
	static String quote(String value) {
		return "\"" + value + "\"";
	}
}