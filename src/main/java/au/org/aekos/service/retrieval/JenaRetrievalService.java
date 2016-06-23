package au.org.aekos.service.retrieval;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.controller.ApiV1RetrievalController.RetrievalResponseHeader;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.EnvironmentDataParams;
import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;

@Service
public class JenaRetrievalService implements RetrievalService {

	static final String SCIENTIFIC_NAME_PLACEHOLDER = "%SCIENTIFIC_NAME_PLACEHOLDER%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET_PLACEHOLDER%";
	private static final String LIMIT_PLACEHOLDER = "%LIMIT_PLACEHOLDER%";
	private static final String LOCATION_ID_PLACEHOLDER = "%LOCATION_ID_PLACEHOLDER%";
	
	@Autowired 
	@Qualifier("dataModel")
	private Model model;
	
	@Autowired
	@Qualifier("darwinCoreQueryTemplate")
	private String darwinCoreQueryTemplate;
	
	@Autowired
	@Qualifier("environmentDataQueryTemplate")
	private String environmentDataQueryTemplate;
	
	@Autowired
	private StubRetrievalService stubDelegate;
	
	@Override
	public List<SpeciesOccurrenceRecord> getSpeciesDataJson(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		return getSpeciesDataJsonPrivate(speciesNames, start, rows);
	}

	@Override
	public void getSpeciesDataCsv(List<String> speciesNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		// TODO write a header?
		for (Iterator<SpeciesOccurrenceRecord> it = getSpeciesDataJsonPrivate(speciesNames, start, rows).iterator();it.hasNext();) {
			SpeciesOccurrenceRecord curr = it.next();
			try {
				responseWriter.write(curr.toCsv());
				if (it.hasNext()) {
					responseWriter.write("\n");
				}
			} catch (IOException e) {
				throw new AekosApiRetrievalException("Failed to write to the supplied writer: " + responseWriter.getClass(), e);
			}
		}
	}

	@Override
	public EnvironmentDataResponse getEnvironmentalDataJson(List<String> speciesNames, List<String> environmentalVariableNames,
			int start, int rows) throws AekosApiRetrievalException {
		return getEnvironmentalDataJsonPrivate(speciesNames, environmentalVariableNames, start, rows);
	}

	@Override
	public RetrievalResponseHeader getEnvironmentalDataCsv(List<String> speciesNames, List<String> environmentalVariableNames, 
			int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		// TODO make real
		return stubDelegate.getEnvironmentalDataCsv(speciesNames, environmentalVariableNames, start, rows, responseWriter);
	}
	
	@Override
	public TraitDataResponse getTraitDataJson(List<String> speciesNames, List<String> traitNames, int start, int count) throws AekosApiRetrievalException {
		return getTraitDataJsonPrivate(speciesNames, traitNames, start, count);
	}

	@Override
	public RetrievalResponseHeader getTraitDataCsv(List<String> speciesNames, List<String> traitNames, int start,
			int count, Writer respWriter) throws AekosApiRetrievalException {
		// TODO write header and deal with varying with schema
		TraitDataResponse result = getTraitDataJsonPrivate(speciesNames, traitNames, start, count);
		for (Iterator<TraitDataRecord> it = result.getResponse().iterator();it.hasNext();) {
			TraitDataRecord curr = it.next();
			try {
				respWriter.write(curr.toCsv());
				if (it.hasNext()) {
					respWriter.write("\n");
				}
			} catch (IOException e) {
				throw new AekosApiRetrievalException("Failed to write to the supplied writer: " + respWriter.getClass(), e);
			}
		}
		return RetrievalResponseHeader.newInstance(result);
	}
	
	private List<SpeciesOccurrenceRecord> getSpeciesDataJsonPrivate(List<String> speciesNames, int start, int rows) {
		// FIXME make species names case insensitive
		List<SpeciesOccurrenceRecord> result = new LinkedList<>();
		String sparql = getProcessedDarwinCoreSparql(speciesNames, start, rows);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new RuntimeException("No results were returned in the solution for the query: " + sparql);
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				result.add(new SpeciesOccurrenceRecord(getDouble(s, "decimalLatitude"),
						getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), getString(s, "locationID"),
						getString(s, "scientificName"), getInt(s, "individualCount"), getString(s, "eventDate"),
						getInt(s, "year"), getInt(s, "month"), getString(s, "bibliographicCitation"),
						getString(s, "datasetID")));
			}
		}
		return result;
	}
	
	private EnvironmentDataResponse getEnvironmentalDataJsonPrivate(List<String> speciesNames, List<String> environmentalVariableNames, int start, 
			int rows) throws AekosApiRetrievalException {
		long startTime = new Date().getTime();
		List<EnvironmentDataRecord> records = new LinkedList<>();
		Set<String> locationIds = getLocations(speciesNames);
		// TODO query using all requires keys (location, time ?)
		String sparql = getProcessedEnvDataSparql(locationIds, start, rows);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new RuntimeException("No results were returned in the solution for the query: " + sparql);
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				records.add(new EnvironmentDataRecord(getDouble(s, "decimalLatitude"),
						getDouble(s, "decimalLongitude"), getString(s, "locationID"), 
						null,11,22,null,null //FIXME get all values into query and extract them
//						getString(s, "eventDate"), getInt(s, "year"), getInt(s, "month"),
//						getString(s, "bibliographicCitation"), getString(s, "datasetID")
						));
			}
		}
		int numFound = records.size(); // FIXME need to get total count
		AbstractParams params = new EnvironmentDataParams(start, rows, speciesNames, environmentalVariableNames);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new EnvironmentDataResponse(responseHeader, records);
	}

	private Set<String> getLocations(List<String> speciesNames) throws AekosApiRetrievalException {
		Set<String> result = new LinkedHashSet<>();
		List<SpeciesOccurrenceRecord> speciesRecords = getSpeciesDataJson(speciesNames, 0, Integer.MAX_VALUE); // FIXME should we page this?
		for (SpeciesOccurrenceRecord currSpeciesRecord : speciesRecords) {
			result.add(currSpeciesRecord.getLocationID());
			// FIXME get more info?
		}
		return result;
	}

	private TraitDataResponse getTraitDataJsonPrivate(List<String> speciesNames, List<String> traitNames, int start, int count) throws AekosApiRetrievalException {
		// FIXME make real
		return stubDelegate.getTraitDataJson(speciesNames, traitNames, start, count);
	}
	
	String getProcessedDarwinCoreSparql(List<String> speciesNames, int offset, int limit) {
		String scientificNameValueList = speciesNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = darwinCoreQueryTemplate
				.replace(SCIENTIFIC_NAME_PLACEHOLDER, scientificNameValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return processedSparql;
	}
	
	private String getProcessedEnvDataSparql(Set<String> locationIds, int offset, int limit) {
		String locationIDValueList = locationIds.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = environmentDataQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return processedSparql;
	}

	private int getInt(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getInt();
	}

	private String getString(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getString();
	}

	private double getDouble(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getDouble();
	}
	
	public void setModel(Model model) {
		this.model = model;
	}

	public void setDarwinCoreQueryTemplate(String darwinCoreQueryTemplate) {
		this.darwinCoreQueryTemplate = darwinCoreQueryTemplate;
	}
}
