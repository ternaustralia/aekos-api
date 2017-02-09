package au.org.aekos.api.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.api.service.auth.AuthStorageService;
import au.org.aekos.api.service.auth.AuthStorageService.KeySummary;
import au.org.aekos.api.service.metric.MetricsStorageService;
import au.org.aekos.api.service.metric.RequestRecorder.RequestType;
import au.org.aekos.api.service.search.SearchService;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/v1")
public class ApiV1MaintenanceController {

	private static final String TURTLE_MEDIA_TYPE = "text/turtle";
	
	@Autowired
	private SearchService searchService;
	
	@Autowired
	@Qualifier("jenaMetricsStorageService")
	private MetricsStorageService metricsService;
	
	@Autowired
	private AuthStorageService authService;
	
	@Value("${aekos-api.maintenance-password}")
	private String maintenancePassword;
	
	@Value("${aekos-api.is-production}")
	private boolean isProd;
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
	@Value("${lucene.index.wpath}")
	private String windowsIndexPath;
    
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
	
	@RequestMapping(path="/getMetricsDump", method=RequestMethod.GET, produces=TURTLE_MEDIA_TYPE)
	@ApiIgnore
    public void getMetricsDump(@RequestParam String password,
    		HttpServletResponse resp, Writer responseWriter) throws IOException {
		if (!maintenancePassword.equals(password)) {
    		write(responseWriter, "These are not the droids you're looking for");
    		resp.setStatus(HttpStatus.FORBIDDEN.value());
		}
		metricsService.writeRdfDump(responseWriter);
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
    	if (isProd || !maintenancePassword.equals(password)) {
    		write(responseWriter, "These are not the droids you're looking for");
    		resp.setStatus(HttpStatus.FORBIDDEN.value());
    		return true;
    	}
    	return false;
    }
}
