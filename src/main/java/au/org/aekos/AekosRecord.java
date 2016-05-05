package au.org.aekos;

public class AekosRecord {

    private final double latCoord;
    private final double longCoord;
    private final String siteId;
    private final String species;
    private final String collectionFormat;
    private final String date;
    private final String year;
    private final String month;
    private final String traitHeight;
    private final String traitGrowth;
    private final String envVariablePh;
    private final String envVariableEc;
    private final String citation;
    private final String metadata;

	public AekosRecord(double latCoord, double longCoord, String siteId, String species, String collectionFormat,
			String date, String year, String month, String traitHeight, String traitGrowth, String envVariablePh,
			String envVariableEc, String citation, String metadata) {
		this.latCoord = latCoord;
		this.longCoord = longCoord;
		this.siteId = siteId;
		this.species = species;
		this.collectionFormat = collectionFormat;
		this.date = date;
		this.year = year;
		this.month = month;
		this.traitHeight = traitHeight;
		this.traitGrowth = traitGrowth;
		this.envVariablePh = envVariablePh;
		this.envVariableEc = envVariableEc;
		this.citation = citation;
		this.metadata = metadata;
	}

	public double getLatCoord() {
		return latCoord;
	}

	public double getLongCoord() {
		return longCoord;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getSpecies() {
		return species;
	}

	public String getCollectionFormat() {
		return collectionFormat;
	}

	public String getDate() {
		return date;
	}

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getTraitHeight() {
		return traitHeight;
	}

	public String getTraitGrowth() {
		return traitGrowth;
	}

	public String getEnvVariablePh() {
		return envVariablePh;
	}

	public String getEnvVariableEc() {
		return envVariableEc;
	}

	public String getCitation() {
		return citation;
	}

	public String getMetadata() {
		return metadata;
	}

	public static AekosRecord deserialiseFrom(String[] fields) {
    	double latCoordField = Double.parseDouble(fields[0]); // FIXME validate
		double longCoordField = Double.parseDouble(fields[1]); // FIXME validate
		String siteIdField = fields[2];
		String speciesField = fields[3];
		String collectionFormatField = fields[4];
		String dateOfRecordingField = fields[5];
		String yearOfRecordingField = fields[6];
		String monthOfRecordingField = fields[7];
		String traightHeightField = fields[8];
		String traitGrowthField = fields[9];
		String envVariablePhField = fields[10];
		String envVariableEcField = fields[11];
		String citationField = fields[12];
		String metadataField = fields[13];
		AekosRecord result = new AekosRecord(latCoordField, longCoordField, 
				siteIdField, speciesField, collectionFormatField, dateOfRecordingField, 
				yearOfRecordingField, monthOfRecordingField, traightHeightField, traitGrowthField, envVariablePhField, envVariableEcField, citationField, 
				metadataField);
		return result;
    }
}
