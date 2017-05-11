package au.org.aekos.api.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
	
	@Autowired private AekosReaderListener readListener;
	@Autowired private AekosProcessorListener processorListener;
	@Autowired private AekosWriterListener writerListener;

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("!!! JOB FINISHED!");
			logger.info("Read errors: " + readListener.getErrorCounter());
			logger.info("Processor errors: " + processorListener.getErrorCounter());
			logger.info("Writer errors: " + writerListener.getErrorCounter());
		}
	}
}
