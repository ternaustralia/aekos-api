package au.org.aekos.api.producer.step.env;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.EnvVarRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;

public class EnvItemProcessor implements ItemProcessor<InputEnvRecord, OutputEnvWrapper> {

	@Override
	public OutputEnvWrapper process(InputEnvRecord item) throws Exception {
		OutputEnvRecord envRecord = new OutputEnvRecord(item);
		// TODO enrich with variable information
		List<EnvVarRecord> variables = Collections.emptyList();
		return new OutputEnvWrapper(envRecord, variables);
	}
}
