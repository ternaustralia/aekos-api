package au.org.aekos.api.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class AekosProcessorListener implements ItemProcessListener<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(AekosProcessorListener.class);
    private int errorCounter = 0;
    
	@Override
	public void beforeProcess(Object item) { }

	@Override
	public void afterProcess(Object item, Object result) { }

	@Override
	public void onProcessError(Object item, Exception e) {
		errorCounter++;
		if (errorCounter % 10000 == 0) {
			logger.error(String.format("Data process problem: %d total processing problems so far", errorCounter));
		}
	}
	
	public int getErrorCounter() {
		return errorCounter;
	}
}