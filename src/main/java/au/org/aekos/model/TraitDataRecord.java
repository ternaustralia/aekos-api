package au.org.aekos.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class TraitDataRecord {

	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String locationID;
    private final String scientificName;
    private final String collectionFormat;
    private final String eventDate;
    private final String year;
    private final String month;
    private final Collection<Entry> traits = new LinkedList<>();
    private final String bibliographicCitation;
    private final String datasetID;

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
    
	public TraitDataRecord(double decimalLatitude, double decimalLongitude, String locationID, String scientificName,
			String collectionFormat, String eventDate, String year, String month, String bibliographicCitation, String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.locationID = locationID;
		this.scientificName = scientificName;
		this.collectionFormat = collectionFormat;
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

	public String getCollectionFormat() {
		return collectionFormat;
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

	public Collection<Entry> getTraits() {
		return Collections.unmodifiableCollection(traits);
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public void addTraitValue(Entry entry) {
		traits.add(entry);
	}
}
