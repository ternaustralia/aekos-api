package au.org.aekos.api.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
public class AekosReaderListener implements ItemReadListener<Object> {

    private static final Logger logger = LoggerFactory.getLogger(AekosReaderListener.class);
    private int errorCounter = 0;
    
	@Override
	public void beforeRead() { }

	@Override
	public void afterRead(Object item) { }

	@Override
	public void onReadError(Exception e) {
		logger.error("Data read problem: Failed whilst reading a record, it will be skipped", e);
		errorCounter++;
	}

	public int getErrorCounter() {
		return errorCounter;
	}
}