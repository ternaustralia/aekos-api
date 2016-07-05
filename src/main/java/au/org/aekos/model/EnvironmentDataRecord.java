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
    private final Collection<EnvironmentalVariable> variables = new LinkedList<>();
    private final String bibliographicCitation;
    private final String samplingProtocol;

    public static class EnvironmentalVariable {
    	private final String name;
    	private final String value;
    	private final String units;
		
    	public EnvironmentalVariable(String name, String value, String units) {
			this.name = name;
			this.value = value;
			this.units = units;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public String getUnits() {
			return units;
		}

		@Override
		public String toString() {
			return "[" + name + "=" + value + " " + units + "]";
		}
    }
    
	public EnvironmentDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String eventDate, int year, int month, String bibliographicCitation, String samplingProtocol) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.geodeticDatum = geodeticDatum;
		this.locationID = locationID;
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

	public String getSamplingProtocol() {
		return samplingProtocol;
	}

	public Collection<EnvironmentalVariable> getVariables() {
		return Collections.unmodifiableCollection(variables);
	}

	public void addVariable(EnvironmentalVariable entry) {
		variables.add(entry);
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
		result.append(quote(samplingProtocol));
		for (EnvironmentalVariable curr : variables) {
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.name));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.value));
		}
		return result.toString();
	}

	private static String quote(String value) {
		return "\"" + value + "\"";
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
}
