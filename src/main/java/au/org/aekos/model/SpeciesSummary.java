package au.org.aekos.model;

public class SpeciesSummary {

	private final String id;
	private String scientificName;
	private final int recordsHeld;

	public SpeciesSummary(String id, String scientificName, int recordsHeld) {
		this.id = id;
		this.scientificName = scientificName;
		this.recordsHeld = recordsHeld;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getId() {
		return id;
	}

	public int getRecordsHeld() {
		return recordsHeld;
	}
}
