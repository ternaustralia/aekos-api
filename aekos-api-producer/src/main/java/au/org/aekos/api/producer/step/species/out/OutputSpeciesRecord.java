package au.org.aekos.api.producer.step.species.out;

import au.org.aekos.api.producer.Utils;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class OutputSpeciesRecord {
	private final InputSpeciesRecord coreRecord;

	public OutputSpeciesRecord(InputSpeciesRecord coreRecord) {
		this.coreRecord = coreRecord;
	}

	public String getId() {
		return Utils.quote(coreRecord.getId());
	}

	public int getIndividualCount() {
		return coreRecord.getIndividualCount();
	}

	public String getLocationID() {
		return Utils.quote(coreRecord.getLocationID());
	}

	public String getScientificName() {
		return Utils.quote(coreRecord.getScientificName());
	}

	public String getTaxonRemarks() {
		return Utils.quote(coreRecord.getTaxonRemarks());
	}

	public String getEventDate() {
		return Utils.quote(coreRecord.getEventDate());
	}

	public static String[] getCsvFields() {
		return new String[] {"id", "locationID", "eventDate", "individualCount", "scientificName", "taxonRemarks"};
	}
}
