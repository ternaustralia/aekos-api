package au.org.aekos.api.model;

public class SpeciesSummary {

	private final String id;
	private final String speciesName;
	private final int recordsHeld;

	public SpeciesSummary(String id, String speciesName, int recordsHeld) {
		this.id = id;
		this.speciesName = speciesName;
		this.recordsHeld = recordsHeld;
	}
	
	public SpeciesSummary(String speciesName, int recordsHeld) {
		this.id = String.valueOf(speciesName.hashCode());
		this.speciesName = speciesName;
		this.recordsHeld = recordsHeld;
	}

	public String getSpeciesName() {
		return speciesName;
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
		result = prime * result + ((speciesName == null) ? 0 : speciesName.hashCode());
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
		if (speciesName == null) {
			if (other.speciesName != null)
				return false;
		} else if (!speciesName.equals(other.speciesName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SpeciesSummary [" + speciesName + ", " + recordsHeld + " records]";
	}
}
