package au.org.aekos.api.producer.step.species.in;

public class Trait {
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
}
