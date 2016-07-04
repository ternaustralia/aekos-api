package au.org.aekos.service.retrieval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import au.org.aekos.controller.ApiV1RetrievalController.RetrievalResponseHeader;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.TraitDataParams;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataRecord.Entry;
import au.org.aekos.model.TraitDataRecordWrapper;
import au.org.aekos.model.TraitDataRecordWrapper.TraitDataRecordKey;
import au.org.aekos.model.TraitDataResponse;

@Service
public class StubRetrievalService implements RetrievalService {

	private static final String TRAIT_FILE = "/au/org/aekos/AEKOS_BCCVL_import_example_traits.csv";
	private static final Logger logger = LoggerFactory.getLogger(StubRetrievalService.class);
	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Override
	public SpeciesDataResponse getSpeciesDataJson(List<String> speciesNames, int start, int rows) throws AekosApiRetrievalException {
    	throw new NotImplementedException();
	}

	@Override
	public RetrievalResponseHeader getSpeciesDataCsv(List<String> speciesNames, int start, int rows, Writer responseWriter) throws AekosApiRetrievalException {
		throw new NotImplementedException();
	}
	
	@Override
	public TraitDataResponse getTraitDataJson(List<String> speciesNames, List<String> traitNames, int start, int rows) throws AekosApiRetrievalException {
		long startTime = new Date().getTime();
		try {
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(TRAIT_FILE), StandardCharsets.UTF_8)));
			reader.readNext(); // Bin the header
			String[] currLine;
			Map<TraitDataRecordKey, TraitDataRecord> interimAggregatingResult = new HashMap<>();
			while ((currLine = reader.readNext()) != null) {
				String[] processedLine = replaceDatePlaceholder(currLine);
				TraitDataRecordWrapper wrapper = TraitDataRecordWrapper.deserialiseFrom(processedLine);
				if (!speciesIsRequested(wrapper.getScientificName(), speciesNames)) {
					continue;
				}
				boolean isTraitFilteringOn = traitNames.size() > 0;
				if (isTraitFilteringOn && !isTraitRequested(wrapper.getTrait(), traitNames)) {
					continue;
				}
				TraitDataRecordKey key = wrapper.getKey();
				TraitDataRecord record = interimAggregatingResult.get(key);
				if (record == null) {
					record = wrapper.toRecord();
					interimAggregatingResult.put(key, record);
				} else {
					record.addTraitValue(new Entry(wrapper.getTrait(), wrapper.getTraitValue()));
				}
			}
			reader.close();
			List<TraitDataRecord> records = new ArrayList<>(interimAggregatingResult.values());
			int numFound = records.size();
			int toIndex = start+rows > numFound ? numFound : start+rows;
			List<TraitDataRecord> recordPage = records.subList(start, toIndex);
			AbstractParams params = new TraitDataParams(start, rows, speciesNames, traitNames);
			ResponseHeader responseHeader = ResponseHeader.newInstance(start, rows, numFound, startTime, params);
			TraitDataResponse result = new TraitDataResponse(responseHeader, recordPage);
			return result;
		} catch (IOException e) {
			String msg = "Server failed to retrieve trait data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}

	@Override
	public RetrievalResponseHeader getTraitDataCsv(List<String> speciesNames, List<String> traitNames, int start, int rows, Writer respWriter)
			throws AekosApiRetrievalException {
		int checkedLimit = (start > 0) ? start : Integer.MAX_VALUE;
		int rowsProcessed = 0;
		TraitDataResponse result = getTraitDataJson(speciesNames, traitNames, start, rows);
		try {
			for (TraitDataRecord curr : result.getResponse()) {
				respWriter.write(curr.toCsv());
				respWriter.write("\n");
				// FIXME doesn't support start
				if (rowsProcessed++ >= checkedLimit) {
					break;
				}
			}
		} catch (IOException e) {
			throw new AekosApiRetrievalException("Failed to get dummy trait data", e);
		}
		return RetrievalResponseHeader.newInstance(result);
	}
	
	@Override
	public EnvironmentDataResponse getEnvironmentalDataJson(List<String> speciesNames, List<String> environmentalVariableNames, int start, int rows)
			throws AekosApiRetrievalException {
		throw new NotImplementedException();
	}
	
	@Override
	public RetrievalResponseHeader getEnvironmentalDataCsv(List<String> speciesNames,
			List<String> environmentalVariableNames, int start, int rows, Writer respWriter)
					throws AekosApiRetrievalException {
		throw new NotImplementedException();
	}
	
	private boolean speciesIsRequested(String currSpeciesName, List<String> speciesNames) {
		for (String currSpeciesParam : speciesNames) {
			if (currSpeciesName.toLowerCase().contains(currSpeciesParam.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean isTraitRequested(String currTraitName, List<String> traitNames) {
		for (String currTraitParam : traitNames) {
			if (currTraitName.toLowerCase().contains(currTraitParam.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	private String[] replaceDatePlaceholder(String[] line) {
		for (int i = 0; i<line.length;i++) {
			String currField = line[i];
			if (currField.contains(DATE_PLACEHOLDER)) {
				line[i] = currField.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
			}
		}
		return line;
	}
}
