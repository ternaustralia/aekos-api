package au.org.aekos.api.service.metric;

import au.org.aekos.api.controller.ApiV1SearchController;
import au.org.aekos.api.service.auth.AekosApiAuthKey;

public interface RequestRecorder {

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
		V1_ALL_SPECIES_DATA_JSON,
		V1_ALL_SPECIES_DATA_CSV,
		V1_TRAIT_DATA_JSON,
		V1_TRAIT_DATA_CSV,
		V1_ENVIRONMENT_DATA_JSON,
		V1_ENVIRONMENT_DATA_CSV;

		public String getFullnamespace() {
			return JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + name();
		}
	}
	
	/**
	 * Record a resource call that has no parameters.
	 * e.g: {@link ApiV1SearchController#getTraitVocab(javax.servlet.http.HttpServletResponse)}
	 * 
	 * @param authKey		key used to make the request
	 * @param reqType		type of request
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType);
	
	/**
	 * Record a resource call that only need to supply species name(s).
	 * e.g: {@link ApiV1SearchController#getSpeciesSummary(String[], javax.servlet.http.HttpServletResponse)}
	 * 
	 * @param authKey		key used to make the request
	 * @param reqType		type of request
	 * @param speciesNames	species name(s) supplied as parameters
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames);
	
	/**
	 * Record a resource call that passes species and paging information.
	 * 
	 * @param authKey	    key used to make the request
	 * @param reqType	    type of request
	 * @param speciesNames	trait/environmental variable name(s) supplied as parameters
	 * @param pageNum	    number of the page (not start)
	 * @param pageSize	    size of the page
	 */
	void recordRequestWithSpecies(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames, int pageNum, int pageSize);
	
	/**
	 * Record a resource call that passes traits/environmental variable names and paging information.
	 * 
	 * @param authKey				key used to make the request
	 * @param reqType				type of request
	 * @param traitOrEnvVarNames	trait/environmental variable name(s) supplied as parameters
	 * @param pageNum				number of the page (not start)
	 * @param pageSize				size of the page
	 */
	void recordRequestWithTraitsOrEnvVars(AekosApiAuthKey authKey, RequestType reqType, String[] traitOrEnvVarNames, int pageNum, int pageSize);
	
	/**
	 * Record a resource call that passes species, traits/environmental variable names and some paging information.
	 * 
	 * @param authKey				key used to make the request
	 * @param reqType				type of request
	 * @param speciesNames			species name(s) supplied as parameters
	 * @param traitOrEnvVarNames	trait/environmental variable name(s) supplied as parameters
	 * @param start					start param supplied
	 * @param rows					rows param supplied
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames, String[] traitOrEnvVarNames, int start, int rows);

	/**
	 * @param authKey				key used to make the request
	 * @param reqType				type of request
	 * @param speciesFragment		fragment of species name that user entered
	 */
	void recordRequestAutocomplete(AekosApiAuthKey authKey, RequestType reqType, String speciesFragment);
}
