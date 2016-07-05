package au.org.aekos.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class TraitDataRecord {

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
    private final Collection<Entry> traits = new LinkedList<>();
    private final String samplingProtocol;

    public static class Entry {
    	private final String trait;
    	private final String traitValue;
		
    	public Entry(String trait, String traitValue) {
			this.trait = trait;
			this.traitValue = traitValue;
		}

		public String getTrait() {
			return trait;
		}

		public String getTraitValue() {
			return traitValue;
		}
    }
    
	public TraitDataRecord(double decimalLatitude, double decimalLongitude, String geodeticDatum, String locationID,
			String scientificName, int individualCount, String eventDate, int year, int month,
			String bibliographicCitation, String datasetID) {
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
		this.samplingProtocol = datasetID;
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

	public String getScientificName() {
		return scientificName;
	}

	public int getIndividualCount() {
		return individualCount;
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

	public Collection<Entry> getTraits() {
		return Collections.unmodifiableCollection(traits);
	}

	public String getDatasetID() {
		return samplingProtocol;
	}
	
	public void addTraitValue(Entry entry) {
		traits.add(entry);
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
		for (Entry curr : traits) {
			// FIXME are we doing variable width file or repeated rows?
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.trait));
			result.append(CSV_SEPARATOR);
			result.append(quote(curr.traitValue));
		}
		return result.toString();
	}

	private String quote(String value) {
		return "\"" + value + "\"";
	}
}
