package au.org.aekos.service.metric;

import java.io.Writer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

@Component
public class MetricsQueue implements MetricsStorageService {

	private static final Logger logger = LoggerFactory.getLogger(MetricsQueue.class);
	
	@Resource(name="metricsInnerQueue")
	private BlockingQueue<MetricsQueueItem> queue;
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, params));
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
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames,
			int start, int rows) {
		try {
			queue.put(new MetricsQueueItem(authKey, reqType, speciesOrTraitOrEnvVarNames, start, rows));
		} catch (InterruptedException e) {
			handleInterruption(e);
		}
	}
	
	private void handleInterruption(InterruptedException e) {
		logger.warn("Interrupted while trying to record metrics", e);
	}

	@Override
	public Map<RequestType, Integer> getRequestSummary() {
		throw new NotImplementedException("This implementation is only intended to support recording requests");
	}

	@Override
	public void writeRdfDump(Writer writer) {
		throw new NotImplementedException("This implementation is only intended to support recording requests");
	}
}
