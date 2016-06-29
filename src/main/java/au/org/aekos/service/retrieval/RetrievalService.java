package au.org.aekos.service.retrieval;

import java.io.Writer;
import java.util.List;

import au.org.aekos.controller.ApiV1RetrievalController.RetrievalResponseHeader;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.TraitDataResponse;

public interface RetrievalService {

	/**
	 * Retrieves JSON serialised Darwin Core compliant records for the specified species.
	 * 
	 * @param speciesNames	names of the species to retrieve data for
	 * @param start			offset to start retrieving records
	 * @param rows			number of rows to return (page size), 0 means all
	 * @return				some metadata and specified number of requested records
	 * @throws AekosApiRetrievalException	when something goes wrong
	 */
	SpeciesDataResponse getSpeciesDataJson(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException;

	/**
	 * Retrieves CSV serialised Darwin Core compliant records for the specified species.
	 * 
	 * @param speciesNames	names of the species to retrieve data for
	 * @param start			offset to start retrieving records
	 * @param rows			number of rows to return (page size), 0 means all
	 * @param respWriter 	writer to write records to
	 * @return				metadata about the response
	 * @throws AekosApiRetrievalException 
	 */
	RetrievalResponseHeader getSpeciesDataCsv(List<String> speciesNames, int start, int rows, Writer respWriter) throws AekosApiRetrievalException;

	/**
	 * Retrieves JSON serialised trait data for the specified species.
	 * 
	 * The traits can be filtered by supplying <code>traitNames</code>. The result data is
	 * Darwin Core (as per {@link #getSpeciesDataJson(List, int, int)} plus trait data.
	 * 
	 * @param speciesNames	names of the species to retrieve data for
	 * @param traitNames	list of trait names to filter by or an empty list to get all traits
	 * @param start			offset to start retrieving records
	 * @param rows			number of rows to return (page size), 0 means all 
	 * @return				some metadata and specified number of records for the specified species filtered by the supplied trait name list
	 * @throws AekosApiRetrievalException when something goes wrong
	 */
	TraitDataResponse getTraitDataJson(List<String> speciesNames, List<String> traitNames, int start, int rows) throws AekosApiRetrievalException;
	
	/**
	 * Retrieves CSV serialised trait data for the specified species.
	 * 
	 * Same as {@link #getTraitDataJson(List, List, int, int)}
	 * 
	 * @param speciesNames	names of the species to retrieve data for
	 * @param traitNames	list of trait names to filter by or an empty list to get all traits
	 * @param start			offset to start retrieving records
	 * @param rows			number of rows to return (page size), 0 means all 
	 * @param respWriter 	writer to write result rows to
	 * @return				metadata about the response
	 * @throws AekosApiRetrievalException when something goes wrong
	 */
	RetrievalResponseHeader getTraitDataCsv(List<String> speciesNames, List<String> traitNames, int start, int rows, Writer respWriter) throws AekosApiRetrievalException;

	/**
	 * Retrieves JSON serialised environmental variable data for the site that the specified species occur in
	 * 
	 * @param speciesNames					names of the species to retrieve data for
	 * @param environmentalVariableNames	environmental variable names to filter by or an empty list to get all variables
	 * @param start							offset to start retrieving records
	 * @param rows							number of rows to return (page size), 0 means all 
	 * @return								some metadata and specified number of records for the specified species filtered by the supplied variable name list
	 * @throws AekosApiRetrievalException 	when something goes wrong
	 */
	EnvironmentDataResponse getEnvironmentalDataJson(List<String> speciesNames, List<String> environmentalVariableNames, int start, int rows) throws AekosApiRetrievalException;

	/**
	 * Retrieves JSON serialised environmental variable data for the site that the specified species occur in
	 * 
	 * @param speciesNames					names of the species to retrieve data for
	 * @param environmentalVariableNames	environmental variable names to filter by or an empty list to get all variables
	 * @param start							offset to start retrieving records
	 * @param rows							number of rows to return (page size), 0 means all 
	 * @param respWriter 					writer to write result rows to
	 * @return								metadata about the response
	 * @throws AekosApiRetrievalException
	 */
	RetrievalResponseHeader getEnvironmentalDataCsv(List<String> speciesNames, List<String> environmentalVariableNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException;
}
