package au.org.aekos.api.producer.step.env.out;

import java.util.Collections;
import java.util.List;

import au.org.aekos.api.producer.step.AttributeRecord;

public class OutputEnvWrapper {
	private final OutputEnvRecord envRecord;
	private final List<AttributeRecord> envVarRecords;

	public OutputEnvWrapper(OutputEnvRecord envRecord, List<AttributeRecord> AttributeRecords) {
		this.envRecord = envRecord;
		this.envVarRecords = AttributeRecords;
	}

	public List<OutputEnvRecord> getEnvRecord() {
		return Collections.singletonList(envRecord);
	}

	public List<AttributeRecord> getEnvVarRecords() {
		return Collections.unmodifiableList(envVarRecords);
	}
}
