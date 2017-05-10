package au.org.aekos.api.producer.step.env.out;

import au.org.aekos.api.producer.Utils;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class EnvVarRecord {
	private final TraitRecord wrappedRecord; // They have the same stuff so saving on code
	
	public EnvVarRecord(String parentId, String name, String value, String units) {
		wrappedRecord = new TraitRecord(parentId, name, value, units);
	}

	public String getParentId() {
		return wrappedRecord.getParentId();
	}

	public String getName() {
		return Utils.quote(wrappedRecord.getName());
	}

	public String getValue() {
		return Utils.quote(wrappedRecord.getValue());
	}

	public String getUnits() {
		return Utils.quote(wrappedRecord.getUnits());
	}

	public static String[] getCsvFields() {
		return TraitRecord.getCsvFields();
	}
}
