package au.org.aekos.service.retrieval;

import java.io.Writer;
import java.util.List;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataResponse;

public interface RetrievalService {

	/**
	 * @param speciesNames
	 * @param limit
	 * @return
	 * @throws AekosApiRetrievalException
	 */
	List<SpeciesOccurrenceRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit) throws AekosApiRetrievalException;

	/**
	 * @param speciesNames
	 * @param limit
	 * @param triggerDownload
	 * @param responseWriter
	 * @throws AekosApiRetrievalException 
	 */
	void getSpeciesDataCsv(List<String> speciesNames, Integer limit, boolean triggerDownload, Writer responseWriter) throws AekosApiRetrievalException;

	/**
	 * @param speciesNames
	 * @param traitNames
	 * @param count 
	 * @param start 
	 * @return
	 * @throws AekosApiRetrievalException 
	 */
	TraitDataResponse getTraitData(List<String> speciesNames, List<String> traitNames, int start, int count) throws AekosApiRetrievalException;

	/**
	 * @param speciesNames
	 * @param environmentalVariableNames
	 * @return
	 * @throws AekosApiRetrievalException 
	 */
	List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames, List<String> environmentalVariableNames) throws AekosApiRetrievalException;
}
