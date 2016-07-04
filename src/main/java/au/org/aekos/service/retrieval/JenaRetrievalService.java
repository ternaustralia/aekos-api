package au.org.aekos.service.retrieval;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.controller.ApiV1RetrievalController.RetrievalResponseHeader;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.EnvironmentDataParams;
import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentDataRecord.EnvironmentalVariable;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataParams;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;

@Service
public class JenaRetrievalService implements RetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(JenaRetrievalService.class);
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
	@Qualifier("darwinCoreCountQueryTemplate")
	private String darwinCoreCountQueryTemplate;
	
	@Autowired
	@Qualifier("environmentDataQueryTemplate")
	private String environmentDataQueryTemplate;
	
	@Autowired
	@Qualifier("environmentDataCountQueryTemplate")
	private String environmentDataCountQueryTemplate;
	
	@Autowired
	private StubRetrievalService stubDelegate;
	
	@Override
	public SpeciesDataResponse getSpeciesDataJson(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		return getSpeciesDataJsonPrivate(speciesNames, start, rows);
	}

	@Override
	public RetrievalResponseHeader getSpeciesDataCsv(List<String> speciesNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		SpeciesDataResponse jsonResponse = getSpeciesDataJsonPrivate(speciesNames, start, rows);
		try {
			responseWriter.write(SpeciesOccurrenceRecord.getCsvHeader() + "\n");
			for (Iterator<SpeciesOccurrenceRecord> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				SpeciesOccurrenceRecord curr = it.next();
				responseWriter.write(curr.toCsv());
				if (it.hasNext()) {
					responseWriter.write("\n");
				}
			}
		} catch (IOException e) {
			throw new AekosApiRetrievalException("Failed to write to the supplied writer: " + responseWriter.getClass(), e);
		}
		return RetrievalResponseHeader.newInstance(jsonResponse);
	}

	@Override
	public EnvironmentDataResponse getEnvironmentalDataJson(List<String> speciesNames, List<String> environmentalVariableNames,
			int start, int rows) throws AekosApiRetrievalException {
		return getEnvironmentalDataJsonPrivate(speciesNames, environmentalVariableNames, start, rows);
	}

	@Override
	public RetrievalResponseHeader getEnvironmentalDataCsv(List<String> speciesNames, List<String> environmentalVariableNames, 
			int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		try {
			EnvironmentDataResponse jsonResponse = getEnvironmentalDataJsonPrivate(speciesNames, environmentalVariableNames, start, rows);
			responseWriter.write(EnvironmentDataRecord.getCsvHeader());
			int maxVars = 0;
			// FIXME is there a way to avoid looping through the records twice?
			for (Iterator<EnvironmentDataRecord> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				EnvironmentDataRecord curr = it.next();
				maxVars = Math.max(maxVars, curr.getVariables().size());
			}
			for (int i = 1; i<=maxVars; i++) {
				responseWriter.write(",\"var" + i + "Name\"");
				responseWriter.write(",\"var" + i + "Value\"");
				responseWriter.write(",\"var" + i + "Units\"");
			}
			responseWriter.write("\n");
			for (Iterator<EnvironmentDataRecord> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				EnvironmentDataRecord curr = it.next();
				responseWriter.write(curr.toCsv());
				if (it.hasNext()) {
					responseWriter.write("\n");
				}
			}
		return RetrievalResponseHeader.newInstance(jsonResponse);
		} catch (IOException e) {
			throw new AekosApiRetrievalException("Failed to write to the supplied writer: " + responseWriter.getClass(), e);
		}
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
	
	private SpeciesDataResponse getSpeciesDataJsonPrivate(List<String> speciesNames, int start, int rows) {
		// FIXME make species names case insensitive (try binding an LCASE(?scientificName) and using that
		long startTime = new Date().getTime();
		List<SpeciesOccurrenceRecord> records = new LinkedList<>();
		String sparql = getProcessedDarwinCoreSparql(speciesNames, start, rows);
		logger.debug("Species data SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (results.hasNext()) {
				for (; results.hasNext();) {
					QuerySolution s = results.next();
					records.add(new SpeciesOccurrenceRecord(getDouble(s, "decimalLatitude"),
							getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), replaceSpaces(getString(s, "locationID")),
							getString(s, "scientificName"), getInt(s, "individualCount"), getString(s, "eventDate"),
							getInt(s, "year"), getInt(s, "month"), getString(s, "bibliographicCitation"),
							getString(s, "samplingProtocol")));
				}
			}
		}
		int numFound = getTotalNumFoundForSpeciesData(speciesNames);
		AbstractParams params = new SpeciesDataParams(start, rows, speciesNames);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new SpeciesDataResponse(responseHeader, records);
	}
	
	private int getTotalNumFoundForSpeciesData(List<String> speciesNames) {
		String sparql = getProcessedDarwinCoreCountSparql(speciesNames);
		logger.debug("Species data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Programmer error: a count query should always return something");
			}
			return getInt(results.next(), "count");
		}
	}

	private EnvironmentDataResponse getEnvironmentalDataJsonPrivate(List<String> speciesNames, List<String> environmentalVariableNames, int start, 
			int rows) throws AekosApiRetrievalException {
		long startTime = new Date().getTime();
		List<EnvironmentDataRecord> records = new LinkedList<>();
		Map<String, LocationInfo> locationIds = getLocations(speciesNames);
		logger.debug(String.format("Found %d locations", locationIds.size()));
		// TODO query using all required keys (location, time ?)
		String sparql = getProcessedEnvDataSparql(locationIds, start, rows);
		AbstractParams params = new EnvironmentDataParams(start, rows, speciesNames, environmentalVariableNames);
		logger.debug("Environmental data SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				int foundNothing = 0;
				ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, foundNothing, startTime, params);
				return new EnvironmentDataResponse(responseHeader, records);
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				processEnvDataSolution(records, locationIds, s);
			}
		}
		int numFound = getTotalNumFoundEnvironmentData(locationIds);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new EnvironmentDataResponse(responseHeader, records);
	}

	private void processEnvDataSolution(List<EnvironmentDataRecord> records, Map<String, LocationInfo> locationIds, QuerySolution s) {
		String locationID = getString(s, "locationID");
		EnvironmentDataRecord record = new EnvironmentDataRecord(getDouble(s, "decimalLatitude"),
			getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), replaceSpaces(locationID),
			getString(s, "eventDate"), getInt(s, "year"), getInt(s, "month"),
			locationIds.get(locationID).bibliographicCitation,
			locationIds.get(locationID).samplingProtocol);
		records.add(record);
		processEnvDataVars(s, record, "rainfallVars");
		processEnvDataVars(s, record, "temperatureVars");
	}

	private void processEnvDataVars(QuerySolution s, EnvironmentDataRecord record, String propName) {
		Resource locationSubject = s.get("s").asResource();
		StmtIterator varsIterator = locationSubject.listProperties(prop(propName));
		while (varsIterator.hasNext()) {
			Resource currVar = varsIterator.next().getResource();
			String name = currVar.getProperty(prop("name")).getString();
			String value = currVar.getProperty(prop("value")).getString();
			String units = currVar.getProperty(prop("units")).getString();
			record.addVariable(new EnvironmentalVariable(name, value, units));
		}
	}

	private Property prop(String localPropName) {
		String namespace = "http://www.aekos.org.au/api/1.0#"; // FIXME move to config
		return model.createProperty(namespace + localPropName);
	}

	private int getTotalNumFoundEnvironmentData(Map<String, LocationInfo> locationIds) {
		// FIXME need to be sure this is all the IDs
		String sparql = getProcessedEnvDataCountSparql(locationIds);
		logger.debug("Environment data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Programmer error: a count query should always return something");
			}
			return getInt(results.next(), "count");
		}
	}

	private class LocationInfo {
		private final String samplingProtocol;
		private final String bibliographicCitation;
		public LocationInfo(String samplingProtocol, String bibliographicCitation) {
			this.samplingProtocol = samplingProtocol;
			this.bibliographicCitation = bibliographicCitation;
		}
	}
	
	private Map<String, LocationInfo> getLocations(List<String> speciesNames) throws AekosApiRetrievalException {
		Map<String, LocationInfo> result = new HashMap<>();
		SpeciesDataResponse speciesRecords = getSpeciesDataJson(speciesNames, 0, Integer.MAX_VALUE); // FIXME should we page this?
		for (SpeciesOccurrenceRecord currSpeciesRecord : speciesRecords.getResponse()) {
			String locationID = currSpeciesRecord.getLocationID();
			LocationInfo item = new LocationInfo(currSpeciesRecord.getSamplingProtocol(), currSpeciesRecord.getBibliographicCitation());
			result.put(locationID, item);
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
	
	private String getProcessedDarwinCoreCountSparql(List<String> speciesNames) {
		String scientificNameValueList = speciesNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = darwinCoreCountQueryTemplate
				.replace(SCIENTIFIC_NAME_PLACEHOLDER, scientificNameValueList);
		return processedSparql;
	}
	
	private String getProcessedEnvDataSparql(Map<String, LocationInfo> locationIds, int offset, int limit) {
		String locationIDValueList = locationIds.keySet().stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = environmentDataQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return processedSparql;
	}
	
	private String getProcessedEnvDataCountSparql(Map<String, LocationInfo> locationIds) {
		String locationIDValueList = locationIds.keySet().stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = environmentDataCountQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList);
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

	static String replaceSpaces(String locationID) {
		return locationID.replace(" ", "%20");
	}
}
