package au.org.aekos.api.loader.service.index;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

public class Trait {
	private static final Gson gson = new Gson();
	private final String name;
	private final String value;
	private final String units;
	
	public Trait(String name, String value, String units) {
		this.name = name;
		this.value = value;
		this.units = units;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getUnits() {
		return units;
	}

	public boolean hasUnits() {
		return StringUtils.isNotBlank(units);
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public static Trait fromJson(String jsonString) {
		return gson.fromJson(jsonString, Trait.class);
	}

	@Override
	public String toString() {
		return String.format("Trait [name=%s, value=%s, units=%s]", name, value, units);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Trait other = (Trait) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
