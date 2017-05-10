package au.org.aekos.api.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;

import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class AekosReaderListener implements ItemReadListener<InputEnvRecord> {

    private static Logger logger = LoggerFactory.getLogger(AekosReaderListener.class);

	@Override
	public void beforeRead() { }

	@Override
	public void afterRead(InputEnvRecord item) { }

	@Override
	public void onReadError(Exception e) {
		logger.error("Data read problem: Failed whilst reading a record, it will be skipped", e);
	}
}