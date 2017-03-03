package au.org.aekos.api.model;

public class SpeciesOccurrenceRecordV1_1 extends SpeciesOccurrenceRecordV1_0 {

	private final String locationName;
    private final String datasetName;

	/**
	 * Construct a record with a scientificName
	 */
	public SpeciesOccurrenceRecordV1_1(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month, String bibliographicCitation,
			String samplingProtocol, String locationName, String datasetName) {
		super(decimalLatitude, decimalLongitude, geodeticDatum, locationID, scientificName, individualCount, eventDate, year, 
				month, bibliographicCitation, samplingProtocol);
		this.locationName = locationName;
		this.datasetName = datasetName;
	}
	
	/**
	 * Construct a record with taxonRemarks
	 */
	public SpeciesOccurrenceRecordV1_1(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			int individualCount, String eventDate, int year, int month, String bibliographicCitation, String samplingProtocol,
			String taxonRemarks, String locationName, String datasetName) {
		super(decimalLatitude, decimalLongitude, geodeticDatum, locationID, individualCount, eventDate, year, month, 
				bibliographicCitation, samplingProtocol, taxonRemarks);
		this.locationName = locationName;
		this.datasetName = datasetName;
	}

	public String getLocationName() {
		return locationName;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public String toCsv() {
		StringBuilder result = new StringBuilder();
		result.append(super.toCsv());
		result.append(CSV_SEPARATOR);
		result.append(quote(locationName));
		result.append(CSV_SEPARATOR);
		result.append(quote(datasetName));
		return result.toString();
	}

	public static String getCsvHeader() {
		StringBuilder result = new StringBuilder();
		result.append(SpeciesOccurrenceRecordV1_0.getCsvHeader());
		result.append(CSV_SEPARATOR);
		result.append(quote("locationName"));
		result.append(CSV_SEPARATOR);
		result.append(quote("datasetName"));
		return result.toString();
	}
}
