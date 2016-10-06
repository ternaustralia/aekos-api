package au.org.aekos.service.metric;

import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.org.aekos.service.auth.AekosApiAuthKey;

@Component
public class MetricsQueue implements RequestRecorder {

	private static final Logger logger = LoggerFactory.getLogger(MetricsQueue.class);
	
	@Resource(name="metricsInnerQueue")
	private BlockingQueue<MetricsQueueItem> queue;

	@Override
	public void recordRequestWithSpecies(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames,
			int pageNum, int pageSize) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, speciesNames, pageNum, pageSize));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}

	@Override
	public void recordRequestWithTraitsOrEnvVars(AekosApiAuthKey authKey, RequestType reqType,
			String[] traitOrEnvVarNames, int pageNum, int pageSize) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, pageNum, pageSize, traitOrEnvVarNames));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}

	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}

	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, speciesNames));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}

	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames, String[] traitOrEnvVarNames, int start, int rows) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, speciesNames, traitOrEnvVarNames, start, rows));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}
	
	@Override
	public void recordRequestAutocomplete(AekosApiAuthKey authKey, RequestType reqType, String speciesFragment) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, speciesFragment));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}
	
	private void handleInterruption(InterruptedException e) {
		logger.warn("Interrupted while trying to record metrics", e);
	}
}
