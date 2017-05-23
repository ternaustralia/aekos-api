package au.org.aekos.api.producer.step;

/**
 * Attribute could be details of a Species Trait or an Environmental Variable
 */
public class AttributeRecord {
	private final String name;
	private final String value;
	private final String units;
	
	public AttributeRecord(String name, String value, String units) {
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
