package au.org.aekos.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.auth.AuthStorageService;
import au.org.aekos.service.auth.AuthStorageService.KeySummary;
import au.org.aekos.service.metric.MetricsStorageService;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;
import au.org.aekos.service.retrieval.IndexLoaderCallback;
import au.org.aekos.service.retrieval.IndexLoaderRecord;
import au.org.aekos.service.retrieval.RetrievalService;
import au.org.aekos.service.search.SearchService;
import au.org.aekos.service.search.load.LoaderClient;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/v1")
public class ApiV1MaintenanceController {

	private static final Logger logger = LoggerFactory.getLogger(ApiV1MaintenanceController.class);
	
	@Autowired
	private RetrievalService retrievalService;
	
	@Autowired
	private SearchService searchService;
	
	@Autowired
	private LoaderClient loader;
	
	@Autowired
	private MetricsStorageService metricsService;
	
	@Autowired
	private AuthStorageService authService;
	
	@Value("${aekos-api.index-load-trigger-password}")
	private String indexLoadPassword;
	
	@Value("${aekos-api.is-production}")
	private boolean isProd;
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.wpath}")
	private String windowsIndexPath;
    
    @RequestMapping(path="/doIndexLoad", method=RequestMethod.GET, produces=MediaType.TEXT_PLAIN_VALUE)
    @ApiIgnore
    public void doIndexLoad(@RequestParam String password,
    		HttpServletResponse resp, Writer responseWriter) {
    	//FIXME this is an interim measure but we might need to make a separate app from this function
    	if (isProdOrPasswordInvalid(password, responseWriter, resp)) {
    		return;
    	}
    	try {
    		int totalRecordCount = retrievalService.getTotalSpeciesRecordsHeld();
			ProgressTracker tracker = new ProgressTracker(10000, totalRecordCount);
			Map<String, Integer> speciesCounts = new HashMap<>();
			// FIXME clear index before starting
			loader.beginLoad();
			retrievalService.getIndexStream(new IndexLoaderCallback() {
				@Override
				public void accept(IndexLoaderRecord record) {
					try {
						loader.addSpeciesTraitTermsToIndex(record.getScientificName(), new LinkedList<>(record.getTraitNames()));
						loader.addSpeciesEnvironmentTermsToIndex(record.getScientificName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
						Integer speciesCount = speciesCounts.get(record.getScientificName());
						if (speciesCount == null) {
							speciesCount = 0;
						}
						speciesCounts.put(record.getScientificName(), ++speciesCount);
					} catch (IOException e) {
						reportIndexLoadFailure(resp, responseWriter, e);
						return;
					}
					tracker.addRecord();
				}
			});
			processSpeciesCounts(speciesCounts);
			loader.endLoad();
			write(responseWriter, tracker.getFinishedMessage());
			String path = indexPath;
			if (SystemUtils.IS_OS_WINDOWS) {
				path = windowsIndexPath;
			}
			write(responseWriter, "Wrote index to " + path);
			write(responseWriter, "You need to restart the app so it can read the new index");
		} catch (Throwable e) {
			reportIndexLoadFailure(resp, responseWriter, e);
		}
    }

	@RequestMapping(path="/doHealthCheck", method=RequestMethod.GET, produces=MediaType.TEXT_PLAIN_VALUE)
    @ApiIgnore
    public void doHealthCheck(@RequestParam String password,
    		HttpServletResponse resp, Writer responseWriter) {
    	if (isProdOrPasswordInvalid(password, responseWriter, resp)) {
    		return;
    	}
    	checkTraitVocabMapping(responseWriter);
    	checkEnvironmentalVariableVocabMapping(responseWriter);
    }

	@RequestMapping(path="/getMetricsSummary", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiIgnore
    public void getMetricsSummary(@RequestParam String password,
    		HttpServletResponse resp, Writer responseWriter) throws IOException {
		if (isProdOrPasswordInvalid(password, responseWriter, resp)) {
    		return;
    	}
		Set<Entry<RequestType, Integer>> reqSummaryEntrySet = metricsService.getRequestSummary().entrySet();
		if (reqSummaryEntrySet.isEmpty()) {
			responseWriter.write("No metrics are recorded.");
			return;
		}
		for (Entry<MetricsStorageService.RequestType, Integer> curr : reqSummaryEntrySet) {
			responseWriter.write(curr.getKey()  + " called " + curr.getValue() + "\n");
		}
	}
	
	@RequestMapping(path="/getAuthSummary", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiIgnore
    public void getAuthSummary(@RequestParam String password,
    		HttpServletResponse resp, Writer responseWriter) throws IOException {
		if (isProdOrPasswordInvalid(password, responseWriter, resp)) {
    		return;
    	}
		KeySummary summary = authService.getSummary();
		responseWriter.write(summary.getDisabledCount()  + " keys are disabled.\n");
		responseWriter.write(summary.getEnabledCount()  + " keys are enabled.\n");
		responseWriter.write(summary.getTotalCount()  + " total keys.\n");
	}
    
	private void checkTraitVocabMapping(Writer responseWriter) {
		write(responseWriter, "== Checking that all trait vocabs have a label ==");
		int traitsChecked = 0;
		for (TraitOrEnvironmentalVariableVocabEntry curr : searchService.getTraitVocabData()) {
    		traitsChecked++;
			if (curr.getLabel() == null) {
    			write(responseWriter, "ERROR: " + curr.getCode() + " doesn't have a label");
    		}
    	}
		write(responseWriter, "Checked " + traitsChecked + " traits.");
		writeBlankLine(responseWriter);
	}
    
	private void checkEnvironmentalVariableVocabMapping(Writer responseWriter) {
		write(responseWriter, "== Checking that all environmental variable vocabs have a label ==");
		int varsChecked = 0;
		for (TraitOrEnvironmentalVariableVocabEntry curr : searchService.getEnvironmentalVariableVocabData()) {
    		varsChecked++;
			if (curr.getLabel() == null) {
    			write(responseWriter, "ERROR: " + curr.getCode() + " doesn't have a label");
    		}
    	}
		write(responseWriter, "Checked " + varsChecked + " environmental variables.");
		writeBlankLine(responseWriter);
	}
	
	/**
     * Writes a message and a new line to the writer
     * 
     * @param responseWriter	writer to write to
     * @param msg				message to write
     */
    private void write(Writer responseWriter, String msg) {
		try {
			responseWriter.write(msg + System.lineSeparator());
		} catch (IOException e) {
			throw new RuntimeException("Failed to write to response writer", e);
		}
	}

    private void writeBlankLine(Writer responseWriter) {
		write(responseWriter, "");
	}
    
	private boolean isProdOrPasswordInvalid(String password, Writer responseWriter, HttpServletResponse resp) {
    	if (isProd || !indexLoadPassword.equals(password)) {
    		write(responseWriter, "These are not the droids you're looking for");
    		resp.setStatus(HttpStatus.FORBIDDEN.value());
    		return true;
    	}
    	return false;
    }
    
	private void reportIndexLoadFailure(HttpServletResponse resp, Writer responseWriter, Throwable e) {
		resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		logger.error("Failed to doIndexLoad", e);
		write(responseWriter, "Failed to do the index load.");
		e.printStackTrace(new PrintWriter(responseWriter));
	}
	
	private void processSpeciesCounts(Map<String, Integer> speciesCounts) throws IOException {
		for (Entry<String, Integer> curr : speciesCounts.entrySet()) {
			loader.addSpecies(curr.getKey(), curr.getValue());
		}
	}
	
    static class ProgressTracker {
    	private final Date start = new Date();
    	private final int logCheckpoint;
    	private final int totalRecordCount;
    	private int processedRecords = 0;
    	
    	public ProgressTracker(int logCheckpoint, int totalRecordCount) {
			this.logCheckpoint = logCheckpoint;
			this.totalRecordCount = totalRecordCount;
		}

		public void addRecord() {
    		processedRecords++;
    		if (processedRecords % logCheckpoint != 0) {
    			return;
    		}
    		long elapsedSeconds = getElapsedSeconds();
    		double processedPercentage = 100.0 * processedRecords / totalRecordCount;
    		double timeLeft = (100 - processedPercentage) * (elapsedSeconds / processedPercentage);
    		String msg = String.format("Processed %d/%d records (%3.1f%%) in %ds with estimated %5.0fs left",
    				processedRecords, totalRecordCount, processedPercentage, elapsedSeconds, timeLeft);
			logger.info(msg);
    	}
    	
    	public String getFinishedMessage() {
    		long elapsedSeconds = getElapsedSeconds();
    		return "Processed " + processedRecords + " records in " + elapsedSeconds + " seconds.";
    	}

		private long getElapsedSeconds() {
			return (new Date().getTime() - start.getTime()) / 1000;
		}
    }
}
