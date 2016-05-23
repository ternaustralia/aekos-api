package au.org.aekos.service.retrieval;

import java.io.Writer;
import java.util.List;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.TraitDataRecord;

public interface RetrievalService {

	/**
	 * @param speciesNames
	 * @param limit
	 * @return
	 * @throws AekosApiRetrievalException
	 */
	List<SpeciesDataRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit) throws AekosApiRetrievalException;

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
	 * @return
	 */
	List<TraitDataRecord> getTraitData(List<String> speciesNames, List<String> traitNames);

	/**
	 * @param speciesNames
	 * @param environmentalVariableNames
	 * @return
	 */
	List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames, List<String> environmentalVariableNames);
}
