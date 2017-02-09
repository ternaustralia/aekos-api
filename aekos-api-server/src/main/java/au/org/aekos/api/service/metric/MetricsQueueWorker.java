package au.org.aekos.api.service.metric;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Pops items off the queue and inserts calls the service to persist them.
 */
@Service
public class MetricsQueueWorker {

	private static final Logger logger = LoggerFactory.getLogger(MetricsQueueWorker.class);
	private final ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	@Resource(name="metricsInnerQueue")
	private BlockingQueue<MetricsQueueItem> queue;
	
	@Autowired
	@Qualifier("jenaMetricsStorageService")
    private MetricsStorageService metricsStore;

	@PostConstruct
	public void init() {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						MetricsQueueItem item = queue.take();
						item.doPersist(metricsStore);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.warn(getClass().getSimpleName() + " thread was interrupted", e);
				}
			}
		});
	}
	
	@PreDestroy
	public void dismantle() {
		executorService.shutdown();
	}
}
