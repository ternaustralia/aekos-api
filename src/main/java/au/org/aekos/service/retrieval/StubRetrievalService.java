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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesOccurrenceRecord;
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
	public List<SpeciesOccurrenceRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit) throws AekosApiRetrievalException {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getSpeciesDataJsonHelper(speciesNames, checkedLimit);
		} catch (IOException e) {
			String msg = "Server failed to retrieve species JSON data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}

	@Override
	public void getSpeciesDataCsv(List<String> speciesNames, Integer limit, boolean triggerDownload,
			Writer responseWriter) throws AekosApiRetrievalException {
		int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		getSpeciesCsvDataHelper(speciesNames, checkedLimit, responseWriter);
		} catch (IOException e) {
			String msg = "Server failed to retrieve species CSV data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}
	
	@Override
	public TraitDataResponse getTraitData(List<String> speciesNames, List<String> traitNames, int start, int rows) throws AekosApiRetrievalException {
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
	    	int elapsedTime = (int) (new Date().getTime() - startTime);
			int totalPages = calculateTotalPages(rows, numFound);
			int pageNumber = calculatePageNumber(start, numFound, totalPages);
			TraitDataResponse result = TraitDataResponse.newInstance(recordPage, start, rows, numFound, speciesNames, 
					traitNames, elapsedTime, pageNumber, totalPages); // FIXME make factory
			return result;
		} catch (IOException e) {
			String msg = "Server failed to retrieve trait data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}

	int calculateTotalPages(int rows, int numFound) {
		return (int)Math.ceil((double)numFound / (double)rows);
	}

	int calculatePageNumber(int start, int numFound, int totalPages) {
		if (start == 0) {
			return 1;
		}
		double decimalProgress = ((double)start+1) / (double)numFound;
		double decimalPageNumber = decimalProgress * (double)totalPages;
		return (int)Math.ceil(decimalPageNumber);
	}

	@Override
	public List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames,
			List<String> environmentalVariableNames) throws AekosApiRetrievalException {
		try {
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/AEKOS_BCCVL_import_example_envVariables.csv"))));
			reader.readNext(); // Bin the header
			String[] currLine;
			List<EnvironmentDataRecord> result = new LinkedList<>();
			while ((currLine = reader.readNext()) != null) {
				String[] processedLine = replaceDatePlaceholder(currLine);
				EnvironmentDataRecord record = EnvironmentDataRecord.deserialiseFrom(processedLine);
				// FIXME support looking up which sites contain the requested species and only return data for them
				result.add(record);
			}
			reader.close();
			return result;
		} catch (IOException e) {
			String msg = "Server failed to retrieve trait data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}
	
	private void getSpeciesCsvDataHelper(List<String> speciesNames, int limit, Writer responseWriter) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/AEKOS_BCCVL_import_example_speciesOccurrence.csv")));
		String currLine;
		int dontCountHeader = -1;
		int lineCounter = dontCountHeader;
		while (lineCounter < limit && (currLine = in.readLine()) != null) {
			boolean isHeaderNotProcessed = lineCounter == dontCountHeader;
			if (isHeaderNotProcessed) {
				responseWriter.write(replaceDatePlaceholder(currLine) + "\n");
				responseWriter.flush();
				lineCounter++;
				continue;
			}
			String speciesNameInCurrLine = currLine.split(",")[3];
			if (!speciesIsRequested(speciesNameInCurrLine, speciesNames)) {
				continue;
			}
			responseWriter.write(replaceDatePlaceholder(currLine) + "\n");
			responseWriter.flush(); // TODO is it efficient to flush every row?
			lineCounter++;
		}
	}
	
	private List<SpeciesOccurrenceRecord> getSpeciesDataJsonHelper(List<String> speciesNames, int limit) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/AEKOS_BCCVL_import_example_speciesOccurrence.csv"))));
		reader.readNext(); // Bin the header
		String[] currLine;
		int lineCounter = 0;
		List<SpeciesOccurrenceRecord> result = new LinkedList<>();
		while (lineCounter < limit && (currLine = reader.readNext()) != null) {
			String[] processedLine = replaceDatePlaceholder(currLine);
			SpeciesOccurrenceRecord record = SpeciesOccurrenceRecord.deserialiseFrom(processedLine);
			if (!speciesIsRequested(record.getScientificName(), speciesNames)) {
				continue;
			}
			result.add(record);
			lineCounter++;
		}
		reader.close();
		return result;
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
	
	private String replaceDatePlaceholder(String line) {
		if (!line.contains(DATE_PLACEHOLDER)) {
			return line;
		}
		return line.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
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
