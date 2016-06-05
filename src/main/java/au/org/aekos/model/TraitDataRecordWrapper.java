package au.org.aekos.model;

import au.org.aekos.model.TraitDataRecord.Entry;

public class TraitDataRecordWrapper {

	private final double decimalLatitude;
    private final double decimalLongitude;
    private final String locationID;
    private final String scientificName;
    private final String collectionFormat;
    private final String eventDate;
    private final String year;
    private final String month;
    private final String trait;
    private final String traitValue;
    private final String bibliographicCitation;
    private final String datasetID;

	public TraitDataRecordWrapper(double decimalLatitude, double decimalLongitude, String locationID,
			String scientificName, String collectionFormat, String eventDate, String year, String month, String trait,
			String traitValue, String bibliographicCitation, String datasetID) {
		this.decimalLatitude = decimalLatitude;
		this.decimalLongitude = decimalLongitude;
		this.locationID = locationID;
		this.scientificName = scientificName;
		this.collectionFormat = collectionFormat;
		this.eventDate = eventDate;
		this.year = year;
		this.month = month;
		this.trait = trait;
		this.traitValue = traitValue;
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

	public String getTrait() {
		return trait;
	}

	public String getTraitValue() {
		return traitValue;
	}

	public String getBibliographicCitation() {
		return bibliographicCitation;
	}

	public String getDatasetID() {
		return datasetID;
	}

	public static TraitDataRecordWrapper deserialiseFrom(String[] fields) {
		double decimalLatitudeField = Double.parseDouble(fields[0]);
		double decimalLongitudeField = Double.parseDouble(fields[1]);
		String locationIdField = fields[2];
		String scientificNameField = fields[3];
		String collectionFormatField = fields[4];
		String eventDateField = fields[5];
		String yearField = fields[6];
		String monthField = fields[7];
		String traitField = fields[8];
		String traitValueField = fields[9];
		String bibliographicCitationField = fields[10];
		String datasetIdField = fields[11];
		TraitDataRecordWrapper result = new TraitDataRecordWrapper(decimalLatitudeField, decimalLongitudeField, locationIdField,
				scientificNameField, collectionFormatField, eventDateField, yearField, monthField, traitField,
				traitValueField, bibliographicCitationField, datasetIdField);
		return result;
    }
	
	public static class TraitDataRecordKey {
		private final double decimalLatitude;
	    private final double decimalLongitude;
	    private final String locationID;
	    private final String scientificName;
	    private final String collectionFormat;
	    private final String eventDate;
	    private final String year;
	    private final String month;
		public TraitDataRecordKey(double decimalLatitude, double decimalLongitude, String locationID,
				String scientificName, String collectionFormat, String eventDate, String year, String month) {
			this.decimalLatitude = decimalLatitude;
			this.decimalLongitude = decimalLongitude;
			this.locationID = locationID;
			this.scientificName = scientificName;
			this.collectionFormat = collectionFormat;
			this.eventDate = eventDate;
			this.year = year;
			this.month = month;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((collectionFormat == null) ? 0 : collectionFormat.hashCode());
			long temp;
			temp = Double.doubleToLongBits(decimalLatitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(decimalLongitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
			result = prime * result + ((locationID == null) ? 0 : locationID.hashCode());
			result = prime * result + ((month == null) ? 0 : month.hashCode());
			result = prime * result + ((scientificName == null) ? 0 : scientificName.hashCode());
			result = prime * result + ((year == null) ? 0 : year.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TraitDataRecordKey other = (TraitDataRecordKey) obj;
			if (collectionFormat == null) {
				if (other.collectionFormat != null)
					return false;
			} else if (!collectionFormat.equals(other.collectionFormat))
				return false;
			if (Double.doubleToLongBits(decimalLatitude) != Double.doubleToLongBits(other.decimalLatitude))
				return false;
			if (Double.doubleToLongBits(decimalLongitude) != Double.doubleToLongBits(other.decimalLongitude))
				return false;
			if (eventDate == null) {
				if (other.eventDate != null)
					return false;
			} else if (!eventDate.equals(other.eventDate))
				return false;
			if (locationID == null) {
				if (other.locationID != null)
					return false;
			} else if (!locationID.equals(other.locationID))
				return false;
			if (month == null) {
				if (other.month != null)
					return false;
			} else if (!month.equals(other.month))
				return false;
			if (scientificName == null) {
				if (other.scientificName != null)
					return false;
			} else if (!scientificName.equals(other.scientificName))
				return false;
			if (year == null) {
				if (other.year != null)
					return false;
			} else if (!year.equals(other.year))
				return false;
			return true;
		}
	}

	public TraitDataRecordKey getKey() {
		return new TraitDataRecordKey(decimalLatitude, decimalLongitude, locationID, scientificName, collectionFormat,
				eventDate, year, month);
	}

	public TraitDataRecord toRecord() {
		TraitDataRecord result = new TraitDataRecord(decimalLatitude, decimalLongitude, locationID, scientificName, collectionFormat,
				eventDate, year, month, bibliographicCitation, datasetID);
		result.addTraitValue(new Entry(trait, traitValue));
		return result;
	}
}