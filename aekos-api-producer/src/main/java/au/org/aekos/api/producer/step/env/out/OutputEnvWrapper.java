package au.org.aekos.api.producer.step.env.out;

import java.util.Collections;
import java.util.List;

public class OutputEnvWrapper {
	private final OutputEnvRecord envRecord;
	private final List<EnvVarRecord> envVarRecords;

	public OutputEnvWrapper(OutputEnvRecord envRecord, List<EnvVarRecord> envVarRecords) {
		this.envRecord = envRecord;
		this.envVarRecords = envVarRecords;
	}

	public List<OutputEnvRecord> getEnvRecord() {
		return Collections.singletonList(envRecord);
	}

	public List<EnvVarRecord> getEnvVarRecords() {
		return Collections.unmodifiableList(envVarRecords);
	}
}
