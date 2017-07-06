package au.org.aekos.api.producer.step.species.out;

import au.org.aekos.api.producer.CleanQuotedValue;

public class SpeciesTraitRecord {
	private final String parentId;
	private final String name;
	private final String value;
	private final String units;
	
	public SpeciesTraitRecord(String parentId, String name, String value, String units) {
		this.parentId = parentId;
		this.name = name;
		this.value = value;
		this.units = units;
	}

	public String getParentId() {
		return new CleanQuotedValue(parentId).getValue();
	}

	public String getName() {
		return new CleanQuotedValue(name).getValue();
	}

	public String getValue() {
		return new CleanQuotedValue(value).getValue();
	}

	public String getUnits() {
		return new CleanQuotedValue(units).getValue();
	}

	public static String[] getCsvFields() {
		return new String[] {"parentId", "name", "value", "units"};
	}
}
