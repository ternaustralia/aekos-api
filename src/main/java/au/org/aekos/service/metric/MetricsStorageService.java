package au.org.aekos.service.metric;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

public interface MetricsStorageService {

	public enum RequestType {
		SPECIES_AUTOCOMPLETE,
		TRAIT_BY_SPECIES,
		SPECIES_BY_TRAIT,
		ENVIRONMENT_BY_SPECIES,
		SPECIES_SUMMARY,
		SPECIES_DATA,
		TRAIT_DATA,
		ENVIRONMENT_DATA;

		public String getFullnamespace() {
			return JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + name();
		}
	}
	
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params);
}
