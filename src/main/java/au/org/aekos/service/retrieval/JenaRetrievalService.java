package au.org.aekos.service.retrieval;

import static au.org.aekos.util.FieldNames.BIBLIOGRAPHIC_CITATION;
import static au.org.aekos.util.FieldNames.DECIMAL_LATITUDE;
import static au.org.aekos.util.FieldNames.DECIMAL_LONGITUDE;
import static au.org.aekos.util.FieldNames.DISTURBANCE_EVIDENCE_VARS;
import static au.org.aekos.util.FieldNames.EVENT_DATE;
import static au.org.aekos.util.FieldNames.GEODETIC_DATUM;
import static au.org.aekos.util.FieldNames.INDIVIDUAL_COUNT;
import static au.org.aekos.util.FieldNames.LANDSCAPE_VARS;
import static au.org.aekos.util.FieldNames.LOCATION_ID;
import static au.org.aekos.util.FieldNames.MONTH;
import static au.org.aekos.util.FieldNames.NO_UNITS_VARS;
import static au.org.aekos.util.FieldNames.SAMPLING_PROTOCOL;
import static au.org.aekos.util.FieldNames.SCIENTIFIC_NAME;
import static au.org.aekos.util.FieldNames.SOIL_VARS;
import static au.org.aekos.util.FieldNames.TAXON_REMARKS;
import static au.org.aekos.util.FieldNames.YEAR;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.Application;
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
import au.org.aekos.model.VisitInfo;

@Service
public class JenaRetrievalService implements RetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(JenaRetrievalService.class);
	static final String SPECIES_NAMES_PLACEHOLDER = "%SPECIES_NAMES_PLACEHOLDER%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET_PLACEHOLDER%";
	private static final String LIMIT_PLACEHOLDER = "%LIMIT_PLACEHOLDER%";
	private static final String LOCATION_ID_PLACEHOLDER = "%LOCATION_ID_PLACEHOLDER%";
	private static final String TRAIT_NAME_PLACEHOLDER = "%TRAIT_NAME_PLACEHOLDER%";
	private static final String ENV_VAR_PLACEHOLDER = "%ENV_VAR_PLACEHOLDER%";
	private static final String EVENT_DATE_PLACEHOLDER = "%EVENT_DATE_PLACEHOLDER%";
	private static final String SWITCH_PLACEHOLDER = "#OFF";
	private static final List<String> ALL_SPECIES = Collections.emptyList();
	private static final Property NAME_PROP = prop("name");
	private static final Property UNITS_PROP = prop("units");
	private static final Property VALUE_PROP = prop("value");
	private static final Property TRAIT_PROP = prop("trait");
	
	@Autowired
	@Qualifier("coreDS")
	private Dataset ds;
	
	@Autowired
	@Qualifier("darwinCoreQueryTemplate")
	private String darwinCoreQueryTemplate;
	
	@Autowired
	@Qualifier("darwinCoreCountQueryTemplate")
	private String darwinCoreCountQueryTemplate;
	
	@Autowired
	@Qualifier("darwinCoreCountAllQueryTemplate")
	private String darwinCoreCountAllQueryTemplate;
	
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
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Data problem: no results were found. "
						+ "Do you have Darwin Core and environmental variable records loaded?");
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				String speciesName = getString(s, "speciesName");
				// env
				DummyEnvironmentDataRecord envRecord = new DummyEnvironmentDataRecord();
				for (Property currVarProp : Arrays.asList(prop(DISTURBANCE_EVIDENCE_VARS), prop(LANDSCAPE_VARS), prop(NO_UNITS_VARS), prop(SOIL_VARS))) {
					processEnvDataVars(Collections.emptyList(), s.get("loc").asResource(), envRecord, currVarProp);
				}
				// traits
				DummyTraitDataRecord traitRecord = new DummyTraitDataRecord();
				processTraitDataVars(s.get("dwr").asResource(), traitRecord, TRAIT_PROP, Collections.emptyList());
				Set<String> traitNames = traitRecord.getTraits().stream()
						.map(e -> e.getName())
						.collect(Collectors.toSet());
				Set<String> envVarNames = envRecord.getVariables().stream()
						.map(e -> e.getName())
						.collect(Collectors.toSet());
				// result
				callback.accept(new IndexLoaderRecord(speciesName, traitNames, envVarNames));
			}
		}
	}
	
	/**
	 * Acts as a dumb container to hold environmental variables so we can index them
	 */
	private class DummyEnvironmentDataRecord extends EnvironmentDataRecord {
		public DummyEnvironmentDataRecord() {
			super(0, 0, "", "", "", 0, 0, "", "");
		}
	}
	
	/**
	 * Acts as a dumb container to hold traits so we can index them
	 */
	private class DummyTraitDataRecord extends TraitDataRecord {
		public DummyTraitDataRecord() {
			super(0, 0, "", "", "", 0, "", 0, 0, "", "");
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
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
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
		if (hasScientificName(s)) {
			return new SpeciesOccurrenceRecord(getDouble(s, DECIMAL_LATITUDE),
				getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), replaceSpaces(getString(s, LOCATION_ID)),
				getString(s, SCIENTIFIC_NAME), getInt(s, INDIVIDUAL_COUNT), getString(s, EVENT_DATE),
				getInt(s, YEAR), getInt(s, MONTH), getString(s, BIBLIOGRAPHIC_CITATION),
				getString(s, SAMPLING_PROTOCOL));
		}
		return new SpeciesOccurrenceRecord(getDouble(s, DECIMAL_LATITUDE),
			getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), replaceSpaces(getString(s, LOCATION_ID)),
			getInt(s, INDIVIDUAL_COUNT), getString(s, EVENT_DATE),
			getInt(s, YEAR), getInt(s, MONTH), getString(s, BIBLIOGRAPHIC_CITATION),
			getString(s, SAMPLING_PROTOCOL), getString(s, TAXON_REMARKS));
	}
	
	private boolean hasScientificName(QuerySolution s) {
		return s.contains(SCIENTIFIC_NAME);
	}

	/*
	 * We can't merge with #processSpeciesDataSolution() because then the JSON response has
	 * an empty 'traits: []' field.
	 */
	private TraitDataRecord processTraitDataSolution(QuerySolution s) {
		if (hasScientificName(s)) {
			return new TraitDataRecord(getDouble(s, DECIMAL_LATITUDE), getDouble(s, DECIMAL_LONGITUDE),
				getString(s, GEODETIC_DATUM), replaceSpaces(getString(s, LOCATION_ID)),
				getString(s, SCIENTIFIC_NAME), getInt(s, INDIVIDUAL_COUNT), getString(s, EVENT_DATE),
				getInt(s, YEAR), getInt(s, MONTH), getString(s, BIBLIOGRAPHIC_CITATION),
				getString(s, SAMPLING_PROTOCOL));
		}
		return new TraitDataRecord(getDouble(s, DECIMAL_LATITUDE),
				getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), replaceSpaces(getString(s, LOCATION_ID)),
				getInt(s, INDIVIDUAL_COUNT), getString(s, EVENT_DATE),
				getInt(s, YEAR), getInt(s, MONTH), getString(s, BIBLIOGRAPHIC_CITATION),
				getString(s, SAMPLING_PROTOCOL), getString(s, TAXON_REMARKS));
	}
	
	private int getTotalNumFoundForSpeciesData(List<String> speciesNames) {
		String sparql = getProcessedDarwinCoreCountSparql(speciesNames);
		logger.debug("Species data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
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
		VisitTracker visitTracker = getVisitInfoFor(speciesNames);
		logger.debug(String.format("Found %d visits", visitTracker.visitSize())); // FIXME is it actually visits yet or still locations?
		String sparql = getProcessedEnvDataSparql(visitTracker, start, rows);
		AbstractParams params = new EnvironmentDataParams(start, rows, speciesNames, environmentalVariableNames);
		logger.debug("Environmental data SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				int foundNothing = 0;
				ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, foundNothing, startTime, params);
				return new EnvironmentDataResponse(responseHeader, records);
			}
			for (; results.hasNext();) {
				QuerySolution s = results.next();
				processEnvDataSolution(environmentalVariableNames, records, visitTracker, s);
			}
		}
		int numFound = getTotalNumFoundForEnvironmentData(environmentalVariableNames, visitTracker);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new EnvironmentDataResponse(responseHeader, records);
	}

	private void processEnvDataSolution(List<String> varNames, List<EnvironmentDataRecord> records, VisitTracker visitTracker, QuerySolution s) {
		String locationID = getString(s, LOCATION_ID);
		String eventDate = getString(s, EVENT_DATE);
		VisitInfo visitInfo = visitTracker.getVisitInfoFor(locationID, eventDate);
		EnvironmentDataRecord record = new EnvironmentDataRecord(getDouble(s, DECIMAL_LATITUDE),
			getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), replaceSpaces(locationID),
			getString(s, EVENT_DATE), getInt(s, YEAR), getInt(s, MONTH),
			visitInfo.getBibliographicCitation(),
			visitInfo.getSamplingProtocol());
		record.addScientificNames(visitInfo.getScientificNames());
		record.addTaxonRemarks(visitInfo.getTaxonRemarks());
		for (Property currVarProp : Arrays.asList(prop(DISTURBANCE_EVIDENCE_VARS), prop(LANDSCAPE_VARS), prop(NO_UNITS_VARS), prop(SOIL_VARS))) {
			processEnvDataVars(varNames, s.get("s").asResource(), record, currVarProp);
		}
		boolean isEnvVarFilterEnabled = varNames.size() > 0;
		if (isEnvVarFilterEnabled && !record.matchesTraitFilter(varNames)) {
			return;
		}
		records.add(record);
	}

	private void processEnvDataVars(List<String> varNames, Resource locationSubject, EnvironmentDataRecord record, Property prop) {
		StmtIterator varsIterator = locationSubject.listProperties(prop);
		boolean isEnvVarFilterEnabled = varNames.size() > 0;
		while (varsIterator.hasNext()) {
			Resource currVar = varsIterator.next().getResource();
			String name = currVar.getProperty(NAME_PROP).getString();
			if (isEnvVarFilterEnabled && !varNames.contains(name)) {
				continue;
			}
			String value = currVar.getProperty(VALUE_PROP).getString();
			String units = currVar.getProperty(UNITS_PROP).getString();
			record.addVariable(new TraitOrEnvironmentalVariable(name, value, units));
		}
	}

	private int getTotalNumFoundForEnvironmentData(List<String> environmentalVariableNames, VisitTracker visitTracker) {
		String sparql = getProcessedEnvDataCountSparql(environmentalVariableNames, visitTracker);
		logger.debug("Environment data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Programmer error: a count query should always return something");
			}
			return getInt(results.next(), "count");
		}
	}

	VisitTracker getVisitInfoFor(List<String> speciesNames) throws AekosApiRetrievalException {
		VisitTracker result = new VisitTracker();
		SpeciesDataResponse speciesRecords = getSpeciesDataJsonPrivate(speciesNames, 0, Integer.MAX_VALUE); // FIXME should we page this?
		for (SpeciesOccurrenceRecord currSpeciesRecord : speciesRecords.getResponse()) {
			String locationID = currSpeciesRecord.getLocationID();
			String eventDate = currSpeciesRecord.getEventDate();
			VisitInfo item = result.getVisitInfoFor(locationID, eventDate);
			if (item == null) {
				item = new VisitInfo(currSpeciesRecord.getSamplingProtocol(), currSpeciesRecord.getBibliographicCitation());
			}
			currSpeciesRecord.appendSpeciesNameTo(item);
			result.addVisitInfo(locationID, eventDate, item);
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
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
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
		processTraitDataVars(s.get("s").asResource(), record, TRAIT_PROP, traitNames);
		boolean isTraitFilterEnabled = traitNames.size() > 0;
		if (isTraitFilterEnabled && !record.matchesTraitFilter(traitNames)) {
			return;
		}
		records.add(record);
	}

	private void processTraitDataVars(Resource speciesEntity, TraitDataRecord record, Property prop, List<String> traitNames) {
		StmtIterator varsIterator = speciesEntity.listProperties(prop);
		boolean isTraitFilterEnabled = traitNames.size() > 0;
		while (varsIterator.hasNext()) {
			Resource currVar = varsIterator.next().getResource();
			String name = currVar.getProperty(NAME_PROP).getString();
			if (isTraitFilterEnabled && !traitNames.contains(name)) {
				continue;
			}
			String value = currVar.getProperty(VALUE_PROP).getString();
			String units = currVar.getProperty(UNITS_PROP).getString();
			record.addTraitValue(new TraitOrEnvironmentalVariable(name, value, units));
		}
	}

	private int getTotalNumFoundForTraitData(List<String> speciesNames, List<String> traitNames) {
		String sparql = getProcessedTraitDataCountSparql(speciesNames, traitNames);
		logger.debug("Trait data count SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Programmer error: a count query should always return something");
			}
			return getInt(results.next(), "count");
		}
	}
	
	private String getProcessedTraitDataCountSparql(List<String> speciesNames, List<String> traitNames) {
		String speciesNamesValueList = speciesNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String traitNameValueList = traitNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));;
		String result = traitDataCountQueryTemplate
				.replace(SPECIES_NAMES_PLACEHOLDER, speciesNamesValueList)
				.replace(TRAIT_NAME_PLACEHOLDER, traitNameValueList);
		return result;
	}
	
	String getProcessedDarwinCoreSparql(List<String> speciesNames, int offset, int limit) {
		String partialResult = darwinCoreQueryTemplate
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		String partialResult2 = handleSpeciesAndOnOffSwitchPlaceholders(speciesNames, partialResult);
		boolean isRetrievingAll = speciesNames.isEmpty();
		if (isRetrievingAll) {
			return partialResult2;
		}
		return partialResult2
			.replace(SWITCH_PLACEHOLDER, "    ");
	}
	
	private String getProcessedDarwinCoreCountSparql(List<String> speciesNames) {
		boolean isRetrievingAll = speciesNames.isEmpty();
		if (isRetrievingAll) {
			return darwinCoreCountAllQueryTemplate;
		}
		return handleSpeciesAndOnOffSwitchPlaceholders(speciesNames, darwinCoreCountQueryTemplate);
	}
	
	private String handleSpeciesAndOnOffSwitchPlaceholders(List<String> speciesNames, String result) {
		String speciesNameValueList = speciesNames.stream()
				.map(e -> sanitise(e))
				.collect(Collectors.joining("\" \"", "\"", "\""));
		String cleanedResult = result
				.replace(SPECIES_NAMES_PLACEHOLDER, speciesNameValueList);
		return cleanedResult;
	}
	
	private String getProcessedEnvDataSparql(VisitTracker visitTracker, int offset, int limit) {
		String locationIDValueList = visitTracker.getLocationIDSparqlParamList();
		String eventDateValueList = visitTracker.getEventDateSparqlParamList();
		String processedSparql = environmentDataQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList)
				.replace(EVENT_DATE_PLACEHOLDER, eventDateValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return processedSparql;
	}
	
	private String getProcessedEnvDataCountSparql(List<String> environmentalVariableNames, VisitTracker visitTracker) {
		String locationIDValueList = visitTracker.getLocationIDSparqlParamList();
		String eventDateValueList = visitTracker.getEventDateSparqlParamList();
		String processedSparql = environmentDataCountQueryTemplate
				.replace(LOCATION_ID_PLACEHOLDER, locationIDValueList)
				.replace(EVENT_DATE_PLACEHOLDER, eventDateValueList);
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

	private static Property prop(String localPropName) {
		String namespace = Application.API_DATA_NAMESPACE;
		return ModelFactory.createDefaultModel().createProperty(namespace + localPropName);
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

	public void setDarwinCoreQueryTemplate(String darwinCoreQueryTemplate) {
		this.darwinCoreQueryTemplate = darwinCoreQueryTemplate;
	}

	static String replaceSpaces(String locationID) {
		String urlEscapedSpaceCharacter = "%20";
		return locationID.replace(" ", urlEscapedSpaceCharacter);
	}

	static String sanitise(String sparqlParam) {
		String sparqlEscapedBackslash = "\\\\";
		String sparqlEscapedDoubleQuote = "\\\"";
		return sparqlParam
				.replace("\\", sparqlEscapedBackslash)
				.replace("\"", sparqlEscapedDoubleQuote);
	}
}
