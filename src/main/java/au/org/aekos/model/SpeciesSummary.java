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
	
	public SpeciesSummary(String scientificName, int recordsHeld) {
		this.id = String.valueOf(scientificName.hashCode());
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((scientificName == null) ? 0 : scientificName.hashCode());
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
		SpeciesSummary other = (SpeciesSummary) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (scientificName == null) {
			if (other.scientificName != null)
				return false;
		} else if (!scientificName.equals(other.scientificName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SpeciesSummary [" + scientificName + ", " + recordsHeld + "]";
	}
}
