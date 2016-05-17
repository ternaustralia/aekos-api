package au.org.aekos.model;

import java.util.LinkedList;
import java.util.List;

public class TraitDataResponse {

	private final List<TraitDataRecord> records = new LinkedList<>();

	public void add(TraitDataRecord traitDataRecord) {
		records.add(traitDataRecord);
	}

	public List<TraitDataRecord> getRecords() {
		return records;
	}
}
