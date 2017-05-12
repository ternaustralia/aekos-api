package au.org.aekos.api.producer.step.env;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.stereotype.Component;

import au.org.aekos.api.producer.step.AttributeRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;

@Component
public class AekosEnvRelationalCsvWriter extends AbstractItemStreamItemWriter<OutputEnvWrapper> {

	private AbstractItemStreamItemWriter<OutputEnvRecord> envWriter;
	private AbstractItemStreamItemWriter<AttributeRecord> variableWriter;

	@Override
	public void write(List<? extends OutputEnvWrapper> items) throws Exception {
		for (OutputEnvWrapper curr : items) {
			envWriter.write(curr.getEnvRecord());
			variableWriter.write(curr.getEnvVarRecords());
		}
	}
	
	@Override
	public void open(ExecutionContext executionContext) {
		super.open(executionContext);
		envWriter.open(executionContext);
		variableWriter.open(executionContext);
	}

	@Override
	public void close() {
		super.close();
		envWriter.close();
		variableWriter.close();
	}

	public void setEnvWriter(AbstractItemStreamItemWriter<OutputEnvRecord> envWriter) {
		this.envWriter = envWriter;
	}

	public void setVariableWriter(AbstractItemStreamItemWriter<AttributeRecord> variableWriter) {
		this.variableWriter = variableWriter;
	}
}
