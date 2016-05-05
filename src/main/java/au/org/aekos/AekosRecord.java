package au.org.aekos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AekosRecord {

    private final double latCoord;
    private final double longCoord;
    private final String siteId;
    private final String species;
    private final String collectionFormat;
    private final String dateOfRecording;
    private final String yearOfRecording;
    private final String monthOfRecording;
    private final String citation;
    private final String metadata;
    private final List<String> traitValues; // TODO do we need fixed length?
    private final List<String> environmentVariableValues;  // TODO do we need fixed length?

    private AekosRecord(double latCoord, double longCoord, String siteId, String species, String collectionFormat,
			String dateOfRecording, String yearOfRecording, String monthOfRecording, String citation, String metadata,
			List<String> traitValues, List<String> environmentVariableValues) {
		this.latCoord = latCoord;
		this.longCoord = longCoord;
		this.siteId = siteId;
		this.species = species;
		this.collectionFormat = collectionFormat;
		this.dateOfRecording = dateOfRecording;
		this.yearOfRecording = yearOfRecording;
		this.monthOfRecording = monthOfRecording;
		this.citation = citation;
		this.metadata = metadata;
		this.traitValues = traitValues;
		this.environmentVariableValues = environmentVariableValues;
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

	public String getDateOfRecording() {
		return dateOfRecording;
	}

	public String getYearOfRecording() {
		return yearOfRecording;
	}

	public String getMonthOfRecording() {
		return monthOfRecording;
	}

	public String getCitation() {
		return citation;
	}

	public String getMetadata() {
		return metadata;
	}

	public List<String> getTraitValues() {
		return Collections.unmodifiableList(traitValues);
	}

	public List<String> getEnvironmentVariableValues() {
		return Collections.unmodifiableList(environmentVariableValues);
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
		String citationField = fields[8];
		String metadataField = fields[9];
		List<String> traitFieldValues = new ArrayList<>();
		for (int i = 10; i < 14; i++) {
			traitFieldValues.add(fields[i]);
		}
		List<String> environmentVariableFieldValues = new ArrayList<>();
		for (int i = 14; i < 18; i++) {
			environmentVariableFieldValues.add(fields[i]);
		}
		AekosRecord result = new AekosRecord(latCoordField, longCoordField, 
				siteIdField, speciesField, collectionFormatField, dateOfRecordingField, 
				yearOfRecordingField, monthOfRecordingField, citationField, 
				metadataField, traitFieldValues, environmentVariableFieldValues);
		return result;
    }
}
