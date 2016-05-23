package au.org.aekos.service.retrieval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesDataRecord;
import au.org.aekos.model.TraitDataRecord;

@Service
public class StubRetrievalService implements RetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(StubRetrievalService.class);
	private static final String DATE_PLACEHOLDER = "[importDate]";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Override
	public List<SpeciesDataRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit) throws AekosApiRetrievalException {
    	int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		return getSpeciesDataJsonHelper(checkedLimit);
		} catch (IOException e) {
			String msg = "Failed to read dummy data from file";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}

	@Override
	public void getSpeciesDataCsv(List<String> speciesNames, Integer limit, boolean triggerDownload,
			Writer responseWriter) throws AekosApiRetrievalException {
		int checkedLimit = (limit != null && limit > 0) ? limit : Integer.MAX_VALUE;
    	try {
    		getSpeciesCsvDataHelper(checkedLimit, responseWriter);
		} catch (IOException e) {
			String msg = "Server failed to retrieve species CSV data";
			logger.error(msg, e);
			throw new AekosApiRetrievalException(msg, e);
		}
	}
	
	@Override
	public List<TraitDataRecord> getTraitData(List<String> speciesNames, List<String> traitNames) {
		List<TraitDataRecord> result = new LinkedList<>();
		result.add(new TraitDataRecord("row1"));
		result.add(new TraitDataRecord("row2"));
		return result;
	}
	
	@Override
	public List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames,
			List<String> environmentalVariableNames) {
		List<EnvironmentDataRecord> result = new LinkedList<>();
		result.add(new EnvironmentDataRecord("row1"));
		return result;
	}
	
	private void getSpeciesCsvDataHelper(int limit, Writer responseWriter) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv")));
		String currLine;
		int dontCountHeader = -1;
		int lineCounter = dontCountHeader;
		while (lineCounter < limit && (currLine = in.readLine()) != null) {
			responseWriter.write(replaceDatePlaceholder(currLine) + "\n");
			responseWriter.flush(); // TODO is it efficient to flush every row?
			lineCounter++;
		}
	}
	
	private String replaceDatePlaceholder(String line) {
		if (!line.contains(DATE_PLACEHOLDER)) {
			return line;
		}
		return line.replace(DATE_PLACEHOLDER, sdf.format(new Date()));
	}
	
	private List<SpeciesDataRecord> getSpeciesDataJsonHelper(int limit) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/au/org/aekos/data.csv"))));
		reader.readNext(); // Bin the header
		String[] currLine;
		int lineCounter = 0;
		List<SpeciesDataRecord> result = new LinkedList<>();
		while (lineCounter < limit && (currLine = reader.readNext()) != null) {
			String[] processedLine = replaceDatePlaceholder(currLine);
			result.add(SpeciesDataRecord.deserialiseFrom(processedLine));
			lineCounter++;
		}
		reader.close();
		return result;
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
