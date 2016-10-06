package au.org.aekos.service.metric;

import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.metric.RequestRecorder.RequestType;

public class MetricsQueueItem {
	
	private final Callback callback;

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequest(authKey, reqType);
			}
		};
	}

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequest(authKey, reqType, speciesNames);
			}
		};
	}

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames,
			String[] traitOrEnvVarNames, int start, int rows) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequest(authKey, reqType, speciesNames, traitOrEnvVarNames, start, rows);
			}
		};
	}

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames, int pageNum, int pageSize) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequestWithSpecies(authKey, reqType, speciesNames, pageNum, pageSize);
			}
		};
	}

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, int pageNum, int pageSize, String[] traitOrEnvVarNames) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequestWithTraitsOrEnvVars(authKey, reqType, traitOrEnvVarNames, pageNum, pageSize);
			}
		};
	}

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, String speciesFragment) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequestAutocomplete(authKey, reqType, speciesFragment);
			}
		};
	}

	public void doPersist(MetricsStorageService metricsStore) {
		callback.doPersist(metricsStore);
	}
	
	private interface Callback {
		void doPersist(MetricsStorageService service);
	}

}
