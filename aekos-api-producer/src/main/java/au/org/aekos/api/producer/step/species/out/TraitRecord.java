package au.org.aekos.api.producer.step.species.out;

import au.org.aekos.api.producer.Utils;

public class TraitRecord {
	private final String parentId;
	private final String name;
	private final String value;
	private final String units;
	
	public TraitRecord(String parentId, String name, String value, String units) {
		this.parentId = parentId;
		this.name = name;
		this.value = value;
		this.units = units;
	}

	public String getParentId() {
		return Utils.quote(parentId);
	}

	public String getName() {
		return Utils.quote(name);
	}

	public String getValue() {
		return Utils.quote(value);
	}

	public String getUnits() {
		return Utils.quote(units);
	}

	public static String[] getCsvFields() {
		return new String[] {"parentId", "name", "value", "units"};
	}
}
