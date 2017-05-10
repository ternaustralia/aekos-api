package au.org.aekos.api.producer.step.species.out;

import java.util.Collections;
import java.util.List;

public class OutputSpeciesWrapper {
	private final OutputSpeciesRecord speciesRecord;
	private final List<TraitRecord> traitRecords;

	public OutputSpeciesWrapper(OutputSpeciesRecord speciesRecord, List<TraitRecord> traitRecords) {
		this.speciesRecord = speciesRecord;
		this.traitRecords = traitRecords;
	}

	public List<OutputSpeciesRecord> getSpeciesRecord() {
		return Collections.singletonList(speciesRecord);
	}

	public List<TraitRecord> getTraitRecords() {
		return traitRecords;
	}
}
