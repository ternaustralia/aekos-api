package au.org.aekos.api.model;

public class SpeciesName {

	private final String name;

	public SpeciesName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return String.valueOf(name.hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SpeciesName other = (SpeciesName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
