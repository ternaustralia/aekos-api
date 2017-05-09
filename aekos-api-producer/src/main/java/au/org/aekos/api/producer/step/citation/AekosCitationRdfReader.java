package au.org.aekos.api.producer.step.citation;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import au.org.aekos.api.producer.step.citation.in.InputCitationRecord;

public class AekosCitationRdfReader implements ItemReader<InputCitationRecord> {

	private static final Logger logger = LoggerFactory.getLogger(AekosCitationRdfReader.class);
	private Dataset ds;
	private String citationDetailsQuery;
	private boolean isInitialised = false;
	private ResultSet theResults;
	private int processedCitationRecords = 0;
	private long start;
	private QueryExecution qexec;
	
	@Override
	public InputCitationRecord read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		checkInit();
		boolean isNoMoreRecords = !theResults.hasNext();
		if (isNoMoreRecords) {
			long elapsed = (now() - start) / 1000;
			logger.info(String.format("Processed %d citation records in %d seconds", processedCitationRecords, elapsed));
			InputCitationRecord poisonPill = null;
			close();
			return poisonPill;
		}
		QuerySolution currSolution = theResults.next();
		try {
			String samplingProtocol = currSolution.getLiteral("samplingProtocol").getString();
			String bibliographicCitation = currSolution.getLiteral("bibliographicCitation").getString();
			String datasetName = currSolution.getLiteral("datasetName").getString();
			processedCitationRecords++;
			return new InputCitationRecord(samplingProtocol, bibliographicCitation, datasetName);
		} catch (NullPointerException e) {
			Iterable<String> iterable = () -> currSolution.varNames();
			Set<String> vars = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toSet());
			throw new RuntimeException("Available vars: " + vars);
		}
	}

	private void close() {
		qexec.close();
	}

	private void checkInit() {
		if (isInitialised) return;
		String sparql = citationDetailsQuery;
		Query query = QueryFactory.create(sparql);
		start = now();
		qexec = QueryExecutionFactory.create(query, ds);
		theResults = qexec.execSelect();
		if (!theResults.hasNext()) {
			throw new IllegalStateException("Data problem: no results were found. "
					+ "Do you have RDF AEKOS data loaded?");
		}
		isInitialised = true;
	}

	private long now() {
		return new Date().getTime();
	}

	public void setDs(Dataset ds) {
		this.ds = ds;
	}

	public void setCitationDetailsQuery(String citationDetailsQuery) {
		this.citationDetailsQuery = citationDetailsQuery;
	}
}
