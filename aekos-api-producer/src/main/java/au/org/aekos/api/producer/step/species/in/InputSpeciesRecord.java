package au.org.aekos.api.producer.step.species.in;

public class InputSpeciesRecord {
	private final String id;
	private final int individualCount;
	private final String locationID;
	private final String scientificName;
	private final String taxonRemarks;

	public InputSpeciesRecord(String id, int individualCount, String locationID, String scientificName, String taxonRemarks) {
		this.id = id;
		this.individualCount = individualCount;
		this.locationID = locationID;
		this.scientificName = scientificName;
		this.taxonRemarks = taxonRemarks;
		if (scientificName == null && taxonRemarks == null) {
			String template = "Data problem: record with id '%s' is missing BOTH scientificName and taxonRemarks";
			throw new IllegalStateException(String.format(template, id));
		}
	}

	public String getId() {
		return id;
	}

	public int getIndividualCount() {
		return individualCount;
	}

	public String getLocationID() {
		return locationID;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getTaxonRemarks() {
		return taxonRemarks;
	}
}
