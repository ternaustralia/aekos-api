package au.org.aekos.service.retrieval;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import au.org.aekos.controller.RetrievalResponseHeader;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.EnvironmentDataParams;
import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataParams;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataParams;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.model.TraitOrEnvironmentalVariable;

@Service
public class JenaRetrievalService implements RetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(JenaRetrievalService.class);
	static final String SCIENTIFIC_NAME_PLACEHOLDER = "%SCIENTIFIC_NAME_PLACEHOLDER%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET_PLACEHOLDER%";
	private static final String LIMIT_PLACEHOLDER = "%LIMIT_PLACEHOLDER%";
	private static final String LOCATION_ID_PLACEHOLDER = "%LOCATION_ID_PLACEHOLDER%";
	private static final String TRAIT_NAME_PLACEHOLDER = "%TRAIT_NAME_PLACEHOLDER%";
	private static final String ENV_VAR_PLACEHOLDER = "%ENV_VAR_PLACEHOLDER%";
	private static final String SWITCH_PLACEHOLDER = "#OFF";
	private static final List<String> ALL_SPECIES = Collections.emptyList();
	
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
	@Qualifier("traitDataCountQueryTemplate")
	private String traitDataCountQueryTemplate;
	
	@Autowired
	@Qualifier("indexLoaderQuery")
	private String indexLoaderQuery;
	
	@Override
	public SpeciesDataResponse getSpeciesDataJson(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		return getSpeciesDataJsonPrivate(speciesNames, start, rows);
	}

	@Override
	public SpeciesDataResponse getAllSpeciesDataJson(int start, int rows) {
		return getSpeciesDataJsonPrivate(ALL_SPECIES, start, rows);
	}
	
	@Override
	public RetrievalResponseHeader getSpeciesDataCsv(List<String> speciesNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		SpeciesDataResponse jsonResponse = getSpeciesDataJsonPrivate(speciesNames, start, rows);
		return transformToCsv(responseWriter, jsonResponse);
	}

	@Override
	public RetrievalResponseHeader getAllSpeciesDataCsv(int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		SpeciesDataResponse jsonResponse = getSpeciesDataJsonPrivate(ALL_SPECIES, start, rows);
		return transformToCsv(responseWriter, jsonResponse);
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
		try {
			TraitDataResponse jsonResponse = getTraitDataJsonPrivate(speciesNames, traitNames, start, count);
			respWriter.write(TraitDataRecord.getCsvHeader());
			int maxTraits = 0;
			// FIXME is there a way to avoid looping through the records twice?
			for (Iterator<TraitDataRecord> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				TraitDataRecord curr = it.next();
				maxTraits = Math.max(maxTraits, curr.getTraits().size());
			}
			for (int i = 1; i<=maxTraits; i++) {
				respWriter.write(",\"trait" + i + "Name\"");
				respWriter.write(",\"trait" + i + "Value\"");
				respWriter.write(",\"trait" + i + "Units\"");
			}
			respWriter.write("\n");
			for (Iterator<TraitDataRecord> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				TraitDataRecord curr = it.next();
				respWriter.write(curr.toCsv());
				if (it.hasNext()) {
					respWriter.write("\n");
				}
			}
			return RetrievalResponseHeader.newInstance(jsonResponse);
		} catch (IOException e) {
			throw new AekosApiRetrievalException("Failed to write to the supplied writer: " + respWriter.getClass(), e);
		}
	}
	
	@Override
	public void getIndexStream(IndexLoaderCallback callback) {
		String sparql = indexLoaderQuery;
		logger.debug("Index loader SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Data problem: no results were found. "
						+ "Do you have Darwin Core and environmental variable records loaded?");
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				String scientificName = getString(s, "scientificName");
				// env
				EnvironmentDataRecord envRecord = new EnvironmentDataRecord(0, 0, "", "", "", 0, 0, "", "");
				processEnvDataVars(Collections.emptyList(), s.get("loc").asResource(), envRecord, "noUnitsVars");
				// traits
				TraitDataRecord traitRecord = new TraitDataRecord(0, 0, "", "", "", 0, "", 0, 0, "", "");
				processTraitDataVars(s.get("dwr").asResource(), traitRecord, "trait", Collections.emptyList());
				Set<String> traitNames = new HashSet<>();
				for (TraitOrEnvironmentalVariable curr : traitRecord.getTraits()) {
					traitNames.add(curr.getName());
				}
				Set<String> envVarNames = new HashSet<>();
				for (TraitOrEnvironmentalVariable curr : envRecord.getVariables()) {
					envVarNames.add(curr.getName());
				}
				// result
				callback.accept(new IndexLoaderRecord(scientificName, traitNames, envVarNames));
			}
		}
	}
	
	@Override
	public int getTotalRecordsHeldForSpeciesName(String speciesName) {
		// TODO could use Lucene for this but might get a bit hard to test/maintain
		return getTotalNumFoundForSpeciesData(Arrays.asList(speciesName));
	}
	
	@Override
	public int getTotalSpeciesRecordsHeld() {
		return getTotalNumFoundForSpeciesData(ALL_SPECIES);
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
					records.add(processSpeciesDataSolution(s));
				}
			}
		}
		int numFound = getTotalNumFoundForSpeciesData(speciesNames);
		AbstractParams params = new SpeciesDataParams(start, rows, speciesNames);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new SpeciesDataResponse(responseHeader, records);
	}
	
	private RetrievalResponseHeader transformToCsv(Writer responseWriter, SpeciesDataResponse jsonResponse)
			throws AekosApiRetrievalException {
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

	private SpeciesOccurrenceRecord processSpeciesDataSolution(QuerySolution s) {
		return new SpeciesOccurrenceRecord(getDouble(s, "decimalLatitude"),
			getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), replaceSpaces(getString(s, "locationID")),
			getString(s, "scientificName"), getInt(s, "individualCount"), getString(s, "eventDate"),
			getInt(s, "year"), getInt(s, "month"), getString(s, "bibliographicCitation"),
			getString(s, "samplingProtocol"));
	}
	
	/*
	 * We can't merge with #processSpeciesDataSolution() because then the JSON response has
	 * an empty 'traits: []' field.
	 */
	private TraitDataRecord processTraitDataSolution(QuerySolution s) {
		return new TraitDataRecord(getDouble(s, "decimalLatitude"),
			getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), replaceSpaces(getString(s, "locationID")),
			getString(s, "scientificName"), getInt(s, "individualCount"), getString(s, "eventDate"),
			getInt(s, "year"), getInt(s, "month"), getString(s, "bibliographicCitation"),
			getString(s, "samplingProtocol"));
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
				processEnvDataSolution(environmentalVariableNames, records, locationIds, s);
			}
		}
		int numFound = getTotalNumFoundForEnvironmentData(environmentalVariableNames, locationIds);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new EnvironmentDataResponse(responseHeader, records);
	}

	private void processEnvDataSolution(List<String> varNames, List<EnvironmentDataRecord> records, Map<String, LocationInfo> locationIds, QuerySolution s) {
		String locationID = getString(s, "locationID");
		EnvironmentDataRecord record = new EnvironmentDataRecord(getDouble(s, "decimalLatitude"),
			getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), replaceSpaces(locationID),
			getString(s, "eventDate"), getInt(s, "year"), getInt(s, "month"),
			locationIds.get(locationID).bibliographicCitation,
			locationIds.get(locationID).samplingProtocol);
		for (String currVarProp : Arrays.asList("disturbanceEvidenceVars", "landscapeVars", "noUnitVars",
				"rainfallVars", "soilVars", "temperatureVars", "windVars")) {
			processEnvDataVars(varNames, s.get("s").asResource(), record, currVarProp);
		}
		boolean isEnvVarFilterEnabled = varNames.size() > 0;
		if (isEnvVarFilterEnabled && !record.matchesTraitFilter(varNames)) {
			return;
		}
		records.add(record);
	}

	private void processEnvDataVars(List<String> varNames, Resource locationSubject, EnvironmentDataRecord record, String propName) {
		StmtIterator varsIterator = locationSubject.listProperties(prop(propName));
		boolean isEnvVarFilterEnabled = varNames.size() > 0;
		while (varsIterator.hasNext()) {
			Resource currVar = varsIterator.next().getResource();
			String name = currVar.getProperty(prop("name")).getString();
			if (isEnvVarFilterEnabled && !varNames.contains(name)) {
				continue;
			}
			String value = currVar.getProperty(prop("value")).getString();
			String units = currVar.getProperty(prop("units")).getString();
			record.addVariable(new TraitOrEnvironmentalVariable(name, value, units));
		}
	}

	private int getTotalNumFoundForEnvironmentData(List<String> environmentalVariableNames, Map<String, LocationInfo> locationIds) {
		// FIXME need to be sure this is all the IDs
		String sparql = getProcessedEnvDataCountSparql(environmentalVariableNames, locationIds);
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
		SpeciesDataResponse speciesRecords = getSpeciesDataJsonPrivate(speciesNames, 0, Integer.MAX_VALUE); // FIXME should we page this?
		for (SpeciesOccurrenceRecord currSpeciesRecord : speciesRecords.getResponse()) {
			String locationID = currSpeciesRecord.getLocationID();
			LocationInfo item = new LocationInfo(currSpeciesRecord.getSamplingProtocol(), currSpeciesRecord.getBibliographicCitation());
			result.put(locationID, item);
		}
		return result;
	}

	private TraitDataResponse getTraitDataJsonPrivate(List<String> speciesNames, List<String> traitNames, int start, int rows) throws AekosApiRetrievalException {
		// FIXME make species names case insensitive (try binding an LCASE(?scientificName) and using that
		long startTime = new Date().getTime();
		List<TraitDataRecord> records = new LinkedList<>();
		String sparql = getProcessedDarwinCoreSparql(speciesNames, start, rows);
		logger.debug("Trait data SPARQL: " + sparql);
		AbstractParams params = new TraitDataParams(start, rows, speciesNames, traitNames);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				int foundNothing = 0;
				ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, foundNothing, startTime, params);
				return new TraitDataResponse(responseHeader, records);
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				processTraitDataSolution(traitNames, records, s);
			}
		}
		boolean isTraitFilterEnabled = traitNames.size() > 0;
		int numFound = isTraitFilterEnabled ? getTotalNumFoundForTraitData(speciesNames, traitNames) : getTotalNumFoundForSpeciesData(speciesNames);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new TraitDataResponse(responseHeader, records);
	}
	
	private void processTraitDataSolution(List<String> traitNames, List<TraitDataRecord> records, QuerySolution s) {
		TraitDataRecord record = processTraitDataSolution(s);
		processTraitDataVars(s.get("s").asResource(), record, "trait", traitNames);
		boolean isTraitFilterEnabled = traitNames.size() > 0;
		if (isTraitFilterEnabled && !record.matchesTraitFilter(traitNames)) {
			return;
		}
		records.add(record);
	}

	private void processTraitDataVars(Resource speciesEntity, TraitDataRecord record, String propName, List<String> traitNames) {
		StmtIterator varsIterator = speciesEntity.listProperties(prop(propName));
		boolean isTraitFilterEnabled = traitNames.size() > 0;
		while (varsIterator.hasNext()) {
			Resource currVar = varsIterator.next().getResource();
			String name = currVar.getProperty(prop("name")).getString();
			if (isTraitFilterEnabled && !traitNames.contains(name)) {
				continue;
			}
			String value = currVar.getProperty(prop("value")).getString();
			String units = currVar.getProperty(prop("units")).getString();
			record.addTraitValue(new TraitOrEnvironmentalVariable(name, value, units));
		}
	}

	private int getTotalNumFoundForTraitData(List<String> speciesNames, List<String> traitNames) {
		String sparql = getProcessedTraitDataCountSparql(speciesNames, traitNames);
		logger.debug("Trait data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Programmer error: a count query should always return something");
			}
			return getInt(results.next(), "count");
		}
	}
	
	private String getProcessedTraitDataCountSparql(List<String> speciesNames, List<String> traitNames) {
		String scientificNameValueList = speciesNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String traitNameValueList = traitNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));;
		String result = traitDataCountQueryTemplate
				.replace(SCIENTIFIC_NAME_PLACEHOLDER, scientificNameValueList)
				.replace(TRAIT_NAME_PLACEHOLDER, traitNameValueList);
		return result;
	}
	
	String getProcessedDarwinCoreSparql(List<String> speciesNames, int offset, int limit) {
		String result = darwinCoreQueryTemplate
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return handleSpeciesAndOnOffSwitchPlaceholders(speciesNames, result);
	}
	
	private String getProcessedDarwinCoreCountSparql(List<String> speciesNames) {
		String result = darwinCoreCountQueryTemplate;
		return handleSpeciesAndOnOffSwitchPlaceholders(speciesNames, result);
	}
	
	private String handleSpeciesAndOnOffSwitchPlaceholders(List<String> speciesNames, String result) {
		boolean isRetrievingAll = speciesNames.isEmpty();
		if (isRetrievingAll) {
			return result;
		}
		String scientificNameValueList = speciesNames.stream()
				.map(e -> sanitise(e))
				.collect(Collectors.joining("\" \"", "\"", "\""));
		result = result
				.replace(SCIENTIFIC_NAME_PLACEHOLDER, scientificNameValueList)
				.replace(SWITCH_PLACEHOLDER, "    ");
		return result;
	}
	
	private String getProcessedEnvDataSparql(Map<String, LocationInfo> locationIds, int offset, int limit) {
		String locationIDValueList = locationIds.keySet().stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = environmentDataQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return processedSparql;
	}
	
	private String getProcessedEnvDataCountSparql(List<String> environmentalVariableNames, Map<String, LocationInfo> locationIds) {
		String locationIDValueList = locationIds.keySet().stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = environmentDataCountQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList);
		boolean isEnvVarFilterNotEnabled = environmentalVariableNames.size() == 0;
		if (isEnvVarFilterNotEnabled) {
			return processedSparql;
		}
		String envVarValueList = environmentalVariableNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));;
		processedSparql = processedSparql
				.replace(ENV_VAR_PLACEHOLDER, envVarValueList)
				.replace(SWITCH_PLACEHOLDER, "    ");
		return processedSparql;
	}

	private Property prop(String localPropName) {
		String namespace = "http://www.aekos.org.au/api/1.0#"; // FIXME move to config
		return model.createProperty(namespace + localPropName);
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

	static String sanitise(String sparqlParam) {
		return sparqlParam
				.replace("\\", "\\\\")
				.replace("\"", "\\\"");
	}
}
