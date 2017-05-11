package au.org.aekos.api.producer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.stereotype.Component;

@Component
public class AekosWriterListener implements ItemWriteListener<Object> {

    private static final Logger logger = LoggerFactory.getLogger(AekosWriterListener.class);
    private int errorCounter = 0;

	public int getErrorCounter() {
		return errorCounter;
	}

	@Override
	public void beforeWrite(List<? extends Object> items) { }

	@Override
	public void afterWrite(List<? extends Object> items) { }

	@Override
	public void onWriteError(Exception e, List<? extends Object> items) {
		logger.error("Data write problem: Failed whilst writing a record chunk, it will be skipped", e);
		errorCounter++;
	}
}