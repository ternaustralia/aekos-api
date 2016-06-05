package au.org.aekos.model;

public class EnvironmentDataRecord {

	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String locationID;
    private final String eventDate;
    private final String year;
    private final String month;
    private final String envVariable;
    private final String envVariableValue;
    private final String bibliographicCitation;
    private final String datasetID;

	public EnvironmentDataRecord(double decimalLatitude, double decimalLongitude, String locationID, String eventDate,
			String year, String month, String envVariable, String envVariableValue, String bibliographicCitation,
			String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.locationID = locationID;
		this.eventDate = eventDate;
		this.year = year;
		this.month = month;
		this.envVariable = envVariable;
		this.envVariableValue = envVariableValue;
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

	public String getEventDate() {
		return eventDate;
	}

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getEnvVariable() {
		return envVariable;
	}

	public String getEnvVariableValue() {
		return envVariableValue;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public static EnvironmentDataRecord deserialiseFrom(String[] fields) {
		int fieldIndex = 0;
		double decimalLatitudeField = Double.parseDouble(fields[fieldIndex++]);
		double decimalLongitudeField = Double.parseDouble(fields[fieldIndex++]);
		String locationIdField = fields[fieldIndex++];
		String eventDateField = fields[fieldIndex++];
		String yearField = fields[fieldIndex++];
		String monthField = fields[fieldIndex++];
		String envVarField = fields[fieldIndex++];
		String envVarValueField = fields[fieldIndex++];
		String bibliographicCitationField = fields[fieldIndex++];
		String datasetIdField = fields[fieldIndex++];
		EnvironmentDataRecord result = new EnvironmentDataRecord(decimalLatitudeField, decimalLongitudeField,
				locationIdField, eventDateField, yearField, monthField, envVarField, envVarValueField,
				bibliographicCitationField, datasetIdField);
		return result;
	}
}
