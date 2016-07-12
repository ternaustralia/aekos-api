package au.org.aekos.model;

public class TraitOrEnvironmentalVariable {
	private final String name;
	private final String value;
	private final String units;
	
	public TraitOrEnvironmentalVariable(String name, String value, String units) {
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

	@Override
	public String toString() {
		return "[" + name + "=" + value + " " + units + "]";
	}
}