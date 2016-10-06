package au.org.aekos.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressTracker {
	private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class);
	private static final long TEN_SECONDS = 10 * 1000;
	private final Date start = new Date();
	private final int totalRecordCount;
	private int processedRecords = 0;
	private long nextLogCheckpoint = start.getTime() + TEN_SECONDS;
	
	public ProgressTracker(int totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
	}

	public void addRecord() {
		processedRecords++;
		if (now() < nextLogCheckpoint) {
			return;
		}
		nextLogCheckpoint += TEN_SECONDS;
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

	private long now() {
		return new Date().getTime();
	}
	
	private long getElapsedSeconds() {
		return (new Date().getTime() - start.getTime()) / 1000;
	}
}