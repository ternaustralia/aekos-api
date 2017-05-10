package au.org.aekos.api.producer.step.env.out;

import java.util.Collections;
import java.util.List;

public class OutputEnvWrapper {
	private final OutputEnvRecord speciesRecord;
	private final List<EnvVarRecord> traitRecords;

	public OutputEnvWrapper(OutputEnvRecord speciesRecord, List<EnvVarRecord> traitRecords) {
		this.speciesRecord = speciesRecord;
		this.traitRecords = traitRecords;
	}

	public List<OutputEnvRecord> getSpeciesRecord() {
		return Collections.singletonList(speciesRecord);
	}

	public List<EnvVarRecord> getTraitRecords() {
		return traitRecords;
	}
}
