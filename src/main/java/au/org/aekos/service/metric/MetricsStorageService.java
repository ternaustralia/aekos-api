package au.org.aekos.service.metric;

import java.io.Writer;
import java.util.Map;

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
	 * Record a retrieval resource call.
	 * 
	 * @param authKey		key used to make the request
	 * @param reqType		type of request
	 * @param params		parameter object that was used in the request
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params);
	
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
	 * Record a resource call that passes an array of string args (species/trait/environmental variable names)
	 * and some paging information.
	 * 
	 * @param authKey						key used to make the request
	 * @param reqType						type of request
	 * @param speciesOrTraitOrEnvVarNames	species/trait/environmental variable name(s) supplied as parameters
	 * @param start							start param supplied
	 * @param rows							rows param supplied
	 */
	void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames, int start, int rows);

	/**
	 * Get a summary of how many times each request type has been called.
	 * 
	 * @return	mapping of request type to call count
	 */
	Map<RequestType, Integer> getRequestSummary();

	/**
	 * Writes the whole model as TURTLE RDF to the supplied writer
	 * 
	 * @param responseWriter	writer to write to
	 */
	void writeRdfDump(Writer writer);
}
