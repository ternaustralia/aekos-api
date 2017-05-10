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

	public String getDatasetName() {
		return Utils.quote(coreRecord.getDatasetName());
	}

	public double getDecimalLatitude() {
		return coreRecord.getDecimalLatitude();
	}

	public double getDecimalLongitude() {
		return coreRecord.getDecimalLongitude();
	}

	public String getEventDate() {
		return Utils.quote(coreRecord.getEventDate());
	}

	public String getGeodeticDatum() {
		return Utils.quote(coreRecord.getGeodeticDatum());
	}

	public int getIndividualCount() {
		return coreRecord.getIndividualCount();
	}

	public String getLocationID() {
		return Utils.quote(coreRecord.getLocationID());
	}

	public String getLocationName() {
		return Utils.quote(coreRecord.getLocationName());
	}

	public int getMonth() {
		return coreRecord.getMonth();
	}

	public String getSamplingProtocol() {
		return Utils.quote(coreRecord.getSamplingProtocol());
	}

	public String getScientificName() {
		return Utils.quote(coreRecord.getScientificName());
	}

	public String getTaxonRemarks() {
		return Utils.quote(coreRecord.getTaxonRemarks());
	}

	public int getYear() {
		return coreRecord.getYear();
	}

	public static String[] getCsvFields() {
		return new String[] {"id", "datasetName", "decimalLatitude", "decimalLongitude", "eventDate", "geodeticDatum", "individualCount",
				"locationID", "locationName", "month", "samplingProtocol", "scientificName", "taxonRemarks", "year"};
	}
}
