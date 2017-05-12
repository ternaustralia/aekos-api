package au.org.aekos.api.producer.step.species.out;

import java.util.Collections;
import java.util.List;

import au.org.aekos.api.producer.step.AttributeRecord;

public class OutputSpeciesWrapper {
	private final OutputSpeciesRecord speciesRecord;
	private final List<AttributeRecord> traitRecords;

	public OutputSpeciesWrapper(OutputSpeciesRecord speciesRecord, List<AttributeRecord> traitRecords) {
		this.speciesRecord = speciesRecord;
		this.traitRecords = traitRecords;
	}

	public List<OutputSpeciesRecord> getSpeciesRecord() {
		return Collections.singletonList(speciesRecord);
	}

	public List<AttributeRecord> getTraitRecords() {
		return traitRecords;
	}
}
