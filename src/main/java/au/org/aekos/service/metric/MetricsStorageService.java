package au.org.aekos.service.metric;

import au.org.aekos.controller.ApiV1SearchController;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

public interface MetricsStorageService {

	public enum RequestType {
		// Search
		V1_TRAIT_VOCAB,
		V1_ENVVAR_VOCAB,
		V1_SPECIES_AUTOCOMPLETE,
		V1_TRAIT_BY_SPECIES,
		V1_SPECIES_BY_TRAIT,
		V1_ENVIRONMENT_BY_SPECIES,
		V1_SPECIES_SUMMARY,
		// Retrieval
		V1_SPECIES_DATA_JSON,
		V1_SPECIES_DATA_CSV,
		V1_TRAIT_DATA_JSON,
		V1_TRAIT_DATA_CSV,
		V1_ENVIRONMENT_DATA_JSON,
		V1_ENVIRONMENT_DATA_CSV;

		public String getFullnamespace() {
			return JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + name();
		}
	}
	
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params);
	
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType);
	
	/**
	 * Used to record resource calls that only need to supply species name(s).
	 * e.g: {@link ApiV1SearchController#getSpeciesSummary(String[], javax.servlet.http.HttpServletResponse)}
	 * 
	 * @param authKey
	 * @param reqType
	 * @param speciesNames
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames);
	
	/**
	 * Used to record resource calls that need to pass an array of string args (species/trait/environmental variable names)
	 * and some paging information.
	 * 
	 * @param authKey
	 * @param reqType
	 * @param speciesOrTraitOrEnvVarNames
	 * @param start
	 * @param rows
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames, int start, int rows);
}
