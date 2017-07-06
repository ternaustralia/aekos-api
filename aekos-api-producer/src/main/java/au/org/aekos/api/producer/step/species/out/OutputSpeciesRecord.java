package au.org.aekos.api.producer.step.species.out;

import au.org.aekos.api.producer.CleanQuotedValue;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class OutputSpeciesRecord {
	private final InputSpeciesRecord coreRecord;

	public OutputSpeciesRecord(InputSpeciesRecord coreRecord) {
		this.coreRecord = coreRecord;
	}

	public String getId() {
		return new CleanQuotedValue(coreRecord.getId()).getValue();
	}

	public int getIndividualCount() {
		return coreRecord.getIndividualCount();
	}

	public String getLocationID() {
		return new CleanQuotedValue(coreRecord.getLocationID()).getValue();
	}

	public String getScientificName() {
		return new CleanQuotedValue(coreRecord.getScientificName()).getValue();
	}

	public String getTaxonRemarks() {
		return new CleanQuotedValue(coreRecord.getTaxonRemarks()).getValue();
	}

	public String getEventDate() {
		return new CleanQuotedValue(coreRecord.getEventDate()).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"id", "locationID", "eventDate", "individualCount", "scientificName", "taxonRemarks"};
	}
}
