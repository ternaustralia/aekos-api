package au.org.aekos.api.service.retrieval;

import static au.org.aekos.api.loader.util.FieldNames.BIBLIOGRAPHIC_CITATION;
import static au.org.aekos.api.loader.util.FieldNames.DECIMAL_LATITUDE;
import static au.org.aekos.api.loader.util.FieldNames.DECIMAL_LONGITUDE;
import static au.org.aekos.api.loader.util.FieldNames.DISTURBANCE_EVIDENCE_VARS;
import static au.org.aekos.api.loader.util.FieldNames.EVENT_DATE;
import static au.org.aekos.api.loader.util.FieldNames.GEODETIC_DATUM;
import static au.org.aekos.api.loader.util.FieldNames.INDIVIDUAL_COUNT;
import static au.org.aekos.api.loader.util.FieldNames.LANDSCAPE_VARS;
import static au.org.aekos.api.loader.util.FieldNames.LOCATION_ID;
import static au.org.aekos.api.loader.util.FieldNames.MONTH;
import static au.org.aekos.api.loader.util.FieldNames.NO_UNITS_VARS;
import static au.org.aekos.api.loader.util.FieldNames.SAMPLING_PROTOCOL;
import static au.org.aekos.api.loader.util.FieldNames.SCIENTIFIC_NAME;
import static au.org.aekos.api.loader.util.FieldNames.SOIL_VARS;
import static au.org.aekos.api.loader.util.FieldNames.TAXON_REMARKS;
import static au.org.aekos.api.loader.util.FieldNames.YEAR;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

import au.org.aekos.api.Application;
import au.org.aekos.api.controller.RetrievalResponseHeader;
import au.org.aekos.api.model.AbstractParams;
import au.org.aekos.api.model.EnvironmentDataParams;
import au.org.aekos.api.model.EnvironmentDataRecord;
import au.org.aekos.api.model.EnvironmentDataResponse;
import au.org.aekos.api.model.ResponseHeader;
import au.org.aekos.api.model.SpeciesDataParams;
import au.org.aekos.api.model.SpeciesDataResponseV1_0;
import au.org.aekos.api.model.SpeciesDataResponseV1_1;
import au.org.aekos.api.model.SpeciesOccurrenceRecordV1_0;
import au.org.aekos.api.model.TraitDataParams;
import au.org.aekos.api.model.TraitDataRecord;
import au.org.aekos.api.model.TraitDataResponse;
import au.org.aekos.api.model.TraitOrEnvironmentalVariable;
import au.org.aekos.api.model.VisitInfo;

@Deprecated
public class JenaRetrievalService implements RetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(JenaRetrievalService.class);
	static final String SPECIES_NAMES_PLACEHOLDER = "%SPECIES_NAMES_PLACEHOLDER%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET_PLACEHOLDER%";
	private static final String LIMIT_PLACEHOLDER = "%LIMIT_PLACEHOLDER%";
	static final String LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER = "%LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER%";
	private static final String TRAIT_NAME_PLACEHOLDER = "%TRAIT_NAME_PLACEHOLDER%";
	private static final String ENV_VAR_PLACEHOLDER = "%ENV_VAR_PLACEHOLDER%";
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
	public SpeciesDataResponseV1_0 getSpeciesDataJsonV1_0(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		return getSpeciesDataJsonPrivate(speciesNames, start, rows);
	}

	@Override
	public SpeciesDataResponseV1_0 getAllSpeciesDataJsonV1_0(int start, int rows) {
		return getSpeciesDataJsonPrivate(ALL_SPECIES, start, rows);
	}
	
	@Override
	public SpeciesDataResponseV1_1 getSpeciesDataJsonV1_1(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpeciesDataResponseV1_1 getAllSpeciesDataJsonV1_1(int start, int rows) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public RetrievalResponseHeader getSpeciesDataCsvV1_0(List<String> speciesNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		SpeciesDataResponseV1_0 jsonResponse = getSpeciesDataJsonPrivate(speciesNames, start, rows);
		return transformToCsv(responseWriter, jsonResponse);
	}

	@Override
	public RetrievalResponseHeader getAllSpeciesDataCsvV1_0(int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		SpeciesDataResponseV1_0 jsonResponse = getSpeciesDataJsonPrivate(ALL_SPECIES, start, rows);
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
	public int getTotalRecordsHeldForSpeciesName(String speciesName) {
		// TODO could use Lucene for this but might get a bit hard to test/maintain
		return getTotalNumFoundForSpeciesData(Arrays.asList(speciesName));
	}
	
	@Override
	public int getTotalSpeciesRecordsHeld() {
		return getTotalNumFoundForSpeciesData(ALL_SPECIES);
	}
	
	private SpeciesDataResponseV1_0 getSpeciesDataJsonPrivate(List<String> speciesNames, int start, int rows) {
		long startTime = new Date().getTime();
		List<SpeciesOccurrenceRecordV1_0> records = new LinkedList<>();
		doDarwinCoreResultStream(speciesNames, start, rows, UrlEncodeSpaces.YES, new DarwinCoreResultStreamCallback() {
			@Override
			public void handleRecord(SpeciesOccurrenceRecordV1_0 record) {
				records.add(record);
			}
		});
		int numFound = getTotalNumFoundForSpeciesData(speciesNames);
		AbstractParams params = new SpeciesDataParams(start, rows, speciesNames);
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
		return new SpeciesDataResponseV1_0(responseHeader, records);
	}

	private interface DarwinCoreResultStreamCallback {
		void handleRecord(SpeciesOccurrenceRecordV1_0 record);
	}
	
	private void doDarwinCoreResultStream(List<String> speciesNames, int start, int rows, UrlEncodeSpaces encodeSpaces, DarwinCoreResultStreamCallback callback) {
		// FIXME make species names case insensitive (try binding an LCASE(?scientificName) and using that
		String sparql = getProcessedDarwinCoreSparql(speciesNames, start, rows);
		logger.debug("Species data SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (results.hasNext()) {
				for (; results.hasNext();) {
					QuerySolution s = results.next();
					SpeciesOccurrenceRecordV1_0 record = processSpeciesDataSolution(s, encodeSpaces);
					callback.handleRecord(record);
				}
			}
		}
	}
	
	private RetrievalResponseHeader transformToCsv(Writer responseWriter, SpeciesDataResponseV1_0 jsonResponse)
			throws AekosApiRetrievalException {
		try {
			responseWriter.write(SpeciesOccurrenceRecordV1_0.getCsvHeader() + "\n");
			for (Iterator<SpeciesOccurrenceRecordV1_0> it = jsonResponse.getResponse().iterator();it.hasNext();) {
				SpeciesOccurrenceRecordV1_0 curr = it.next();
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

	private enum UrlEncodeSpaces {
		YES,
		NO;
		
		public boolean areSpacesEncoded() {
			return YES == this;
		}
	}
	
	private SpeciesOccurrenceRecordV1_0 processSpeciesDataSolution(QuerySolution s, UrlEncodeSpaces encodeSpaces) {
		String locationID = encodeSpaces.areSpacesEncoded() ? replaceSpaces(getString(s, LOCATION_ID)) : getString(s, LOCATION_ID);
		if (hasScientificName(s)) {
			return new SpeciesOccurrenceRecordV1_0(getDouble(s, DECIMAL_LATITUDE),
				getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), locationID,
				getString(s, SCIENTIFIC_NAME), getInt(s, INDIVIDUAL_COUNT), getString(s, EVENT_DATE),
				getInt(s, YEAR), getInt(s, MONTH), getString(s, BIBLIOGRAPHIC_CITATION),
				getString(s, SAMPLING_PROTOCOL));
		}
		return new SpeciesOccurrenceRecordV1_0(getDouble(s, DECIMAL_LATITUDE),
			getDouble(s, DECIMAL_LONGITUDE), getString(s, GEODETIC_DATUM), locationID,
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
		logger.debug(String.format("Found %d visits", visitTracker.visitSize()));
		AbstractParams params = new EnvironmentDataParams(start, rows, speciesNames, environmentalVariableNames);
		if (visitTracker.isEmpty()) {
			return emptyEnvDataResponse(start, rows, startTime, records, params);
		}
		String sparql = getProcessedEnvDataSparql(visitTracker, start, rows);
		logger.debug("Environmental data SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				return emptyEnvDataResponse(start, rows, startTime, records, params);
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

	private EnvironmentDataResponse emptyEnvDataResponse(int start, int rows, long startTime,
			List<EnvironmentDataRecord> records, AbstractParams params) {
		int foundNothing = 0;
		ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, foundNothing, startTime, params);
		return new EnvironmentDataResponse(responseHeader, records);
	}

	private void processEnvDataSolution(List<String> varNames, List<EnvironmentDataRecord> records, VisitTracker visitTracker, QuerySolution s) {
		String locationID = getString(s, LOCATION_ID);
		String eventDate = getString(s, EVENT_DATE);
		VisitInfo visitInfo = visitTracker.getVisitInfoFor(locationID, eventDate);
		if (visitInfo == null) {
			String msg = String.format("Couldn't find visit info for '%s'@'%s'", locationID, eventDate);
			throw new RuntimeException(msg);
		}
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
		doDarwinCoreResultStream(speciesNames, 0, Integer.MAX_VALUE, UrlEncodeSpaces.NO, new DarwinCoreResultStreamCallback() {
			@Override
			public void handleRecord(SpeciesOccurrenceRecordV1_0 currSpeciesRecord) {
				String locationID = currSpeciesRecord.getLocationID();
				String eventDate = currSpeciesRecord.getEventDate();
				VisitInfo item = result.getVisitInfoFor(locationID, eventDate);
				if (item == null) {
					item = new VisitInfo(currSpeciesRecord.getSamplingProtocol(), currSpeciesRecord.getBibliographicCitation());
				}
				currSpeciesRecord.appendSpeciesNameTo(item);
				result.addVisitInfo(locationID, eventDate, item);
			}
		});
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
	
	String getProcessedEnvDataSparql(VisitTracker visitTracker, int offset, int limit) {
		String locationIDAndEventDateValueList = visitTracker.getLocationIDAndEventDateSparqlParamList();
		String result = environmentDataQueryTemplate
				.replace(LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER, locationIDAndEventDateValueList)
				.replace(OFFSET_PLACEHOLDER, String.valueOf(offset))
				.replace(LIMIT_PLACEHOLDER, String.valueOf(limit == 0 ? Integer.MAX_VALUE : limit));
		return result;
	}
	
	String getProcessedEnvDataCountSparql(List<String> environmentalVariableNames, VisitTracker visitTracker) {
		String locationIDAndEventDateValueList = visitTracker.getLocationIDAndEventDateSparqlParamList();
		String processedSparql = environmentDataCountQueryTemplate
				.replace(LOCATION_ID_AND_EVENT_DATE_PLACEHOLDER, locationIDAndEventDateValueList);
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

	void setDarwinCoreQueryTemplate(String darwinCoreQueryTemplate) {
		this.darwinCoreQueryTemplate = darwinCoreQueryTemplate;
	}

	void setEnvironmentDataQueryTemplate(String environmentDataQueryTemplate) {
		this.environmentDataQueryTemplate = environmentDataQueryTemplate;
	}

	void setEnvironmentDataCountQueryTemplate(String environmentDataCountQueryTemplate) {
		this.environmentDataCountQueryTemplate = environmentDataCountQueryTemplate;
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

	@Override
	public RetrievalResponseHeader getSpeciesDataCsvV1_1(List<String> speciesNames, int start, int rows, Writer respWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RetrievalResponseHeader getAllSpeciesDataCsvV1_1(int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}
}
