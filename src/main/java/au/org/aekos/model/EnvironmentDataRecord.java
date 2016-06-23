package au.org.aekos.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class EnvironmentDataRecord {

	private static final String CSV_SEPARATOR = ",";
	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String geodeticDatum;
    private final String locationID;
    private final String eventDate;
    private final int year;
    private final int month;
    private final Collection<Entry> variables = new LinkedList<>();
    private final String bibliographicCitation;
    private final String datasetID;

    public static class Entry {
    	private final String environmentalVariable;
    	private final String environmentalVariableValue;
		
    	public Entry(String environmentalVariable, String environmentalVariableValue) {
			this.environmentalVariable = environmentalVariable;
			this.environmentalVariableValue = environmentalVariableValue;
		}

		public String getEnvironmentalVariable() {
			return environmentalVariable;
		}

		public String getEnvironmentalVariableValue() {
			return environmentalVariableValue;
		}
    }
    
	public EnvironmentDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String eventDate, int year, int month, String bibliographicCitation, String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
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

	public String getGeodeticDatum() {
		return geodeticDatum;
	}

	public String getLocationID() {
		return locationID;
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

	public Collection<Entry> getVariables() {
		return Collections.unmodifiableCollection(variables);
	}

	public void addVariable(Entry entry) {
		variables.add(entry);
	}
	
	public static EnvironmentDataRecord deserialiseFrom(String[] fields) {
		int fieldIndex = 0;
		double decimalLatitudeField = Double.parseDouble(fields[fieldIndex++]);
		double decimalLongitudeField = Double.parseDouble(fields[fieldIndex++]);
		String geodeticDatumField = fields[fieldIndex++];
		String locationIdField = fields[fieldIndex++];
		String eventDateField = fields[fieldIndex++];
		int yearField = Integer.parseInt(fields[fieldIndex++]);
		int monthField = Integer.parseInt(fields[fieldIndex++]);
		String envVarField = fields[fieldIndex++];
		String envVarValueField = fields[fieldIndex++];
		String bibliographicCitationField = fields[fieldIndex++];
		String datasetIdField = fields[fieldIndex++];
		EnvironmentDataRecord result = new EnvironmentDataRecord(decimalLatitudeField, decimalLongitudeField,
				geodeticDatumField, locationIdField, eventDateField, yearField, monthField, bibliographicCitationField, datasetIdField);
		result.addVariable(new Entry(envVarField, envVarValueField));
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
		result.append(quote(eventDate));
		result.append(CSV_SEPARATOR);
		result.append(year);
		result.append(CSV_SEPARATOR);
		result.append(month);
		result.append(CSV_SEPARATOR);
		result.append(quote(bibliographicCitation));
		result.append(CSV_SEPARATOR);
		result.append(quote(datasetID));
		for (Entry curr : variables) {
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.environmentalVariable));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.environmentalVariableValue));
		}
		return result.toString();
	}

	private String quote(String value) {
		return "\"" + value + "\"";
	}
}
