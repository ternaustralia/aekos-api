package au.org.aekos.service.metric;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;

public class MetricsQueueItem {
	
	private final Callback callback;

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequest(authKey, reqType, params);
			}
		};
	}

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

	public MetricsQueueItem(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames,
			int start, int rows) {
		this.callback = new Callback() {
			@Override
			public void doPersist(MetricsStorageService service) {
				service.recordRequest(authKey, reqType, speciesOrTraitOrEnvVarNames, start, rows);
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
