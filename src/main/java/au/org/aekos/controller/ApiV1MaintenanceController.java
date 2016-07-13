package au.org.aekos.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;

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

import au.org.aekos.service.retrieval.IndexLoaderCallback;
import au.org.aekos.service.retrieval.IndexLoaderRecord;
import au.org.aekos.service.retrieval.RetrievalService;
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
	private LoaderClient loader;
	
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
    		HttpServletResponse resp, Writer responseWriter) throws IOException {
    	//FIXME this is an interim measure but we might need to make a separate app from this function
    	if (isProd || !indexLoadPassword.equals(password)) {
    		responseWriter.write("These are not the droids you're looking for");
    		resp.setStatus(HttpStatus.FORBIDDEN.value());
    		return;
    	}
    	ProgressTracker tracker = new ProgressTracker();
    	loader.beginLoad();
    	retrievalService.getIndexStream(new IndexLoaderCallback() {
			@Override
			public void accept(IndexLoaderRecord record) {
				try {
					loader.addSpeciesTraitTermsToIndex(record.getScientificName(), new LinkedList<>(record.getTraitNames()));
					loader.addSpeciesEnvironmentTermsToIndex(record.getScientificName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
				} catch (IOException e) {
					logger.error("Failed to do index load", e);
					e.printStackTrace(new PrintWriter(responseWriter));
					return;
				}
				tracker.addRecord();
			}
		});
    	loader.endLoad();
		responseWriter.write(tracker.getFinishedMessage());
		String path = indexPath;
		if (SystemUtils.IS_OS_WINDOWS) {
			path = windowsIndexPath;
		}
		responseWriter.write("Wrote index to " + path + System.lineSeparator());
    }
    
    private class ProgressTracker {
    	private final Date start = new Date();
    	private int processedRecords = 0;
    	
    	public void addRecord() {
    		processedRecords++;
    	}
    	
    	public String getFinishedMessage() {
    		long elapsedSeconds = (new Date().getTime() - start.getTime()) / 1000;
    		return "Processed " + processedRecords + " records in " + elapsedSeconds + " seconds.\n";
    	}
    }
}
