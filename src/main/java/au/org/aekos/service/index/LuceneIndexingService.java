package au.org.aekos.service.index;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.controller.ProgressTracker;
import au.org.aekos.service.search.load.LoaderClient;

@Service
public class LuceneIndexingService implements IndexingService {

	private static final Logger logger = LoggerFactory.getLogger(LuceneIndexingService.class);
	
	@Autowired
	@Qualifier("coreDS")
	private Dataset ds;
	
	@Autowired
	private LoaderClient loader;

	@Autowired
	@Qualifier("darwinCoreAndTraitsQuery")
	private String darwinCoreAndTraitsQuery;
	
	@Override
	public String doIndexing() throws IOException {
//		int totalRecordCount = retrievalService.getTotalSpeciesRecordsHeld();
		int totalRecordCount = 1000; // FIXME
		Map<String, Integer> speciesCounts = new HashMap<>();
		loader.beginLoad();
		loader.deleteAll();
		ProgressTracker tracker = new ProgressTracker(totalRecordCount);
		getIndexStream(new IndexLoaderCallback() {
			@Override
			public void accept(IndexLoaderRecord record) {
				try {
					loader.addSpeciesTraitTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getTraitNames()));
					loader.addSpeciesEnvironmentTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
					Integer speciesCount = speciesCounts.get(record.getSpeciesName());
					if (speciesCount == null) {
						speciesCount = 0;
					}
					speciesCounts.put(record.getSpeciesName(), ++speciesCount);
				} catch (IOException e) {
					throw new RuntimeException("Failed to add a record to the index: " + record.toString(), e);
				}
				tracker.addRecord();
			}
		});
		processSpeciesCounts(speciesCounts);
		loader.endLoad();
		return tracker.getFinishedMessage();
	}
	
	private void getIndexStream(IndexLoaderCallback callback) {
		String sparql = darwinCoreAndTraitsQuery;
//		logger.debug("Index loader SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		long now = new Date().getTime();
		long start = now;
		long lastCheckpoint = start;
		long checkpointSize = 2000;
		int processed = 0;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			Iterator<Triple> results = qexec.execConstructTriples();
			if (!results.hasNext()) {
				throw new IllegalStateException("Data problem: no results were found. "
						+ "Do you have RDF AEKOS data loaded?");
			}
			for (; results.hasNext();) {
				Triple s = results.next();
				processed++;
				if (now >= (lastCheckpoint + checkpointSize)) {
					lastCheckpoint = now;
					logger.info(String.format("Processed %d records", processed));
				}
//				String speciesName = getString(s, "speciesName");
//				// env
//				DummyEnvironmentDataRecord envRecord = new DummyEnvironmentDataRecord();
//				for (Property currVarProp : Arrays.asList(prop(DISTURBANCE_EVIDENCE_VARS), prop(LANDSCAPE_VARS), prop(NO_UNITS_VARS), prop(SOIL_VARS))) {
//					processEnvDataVars(Collections.emptyList(), s.get("loc").asResource(), envRecord, currVarProp);
//				}
//				// traits
//				DummyTraitDataRecord traitRecord = new DummyTraitDataRecord();
//				processTraitDataVars(s.get("dwr").asResource(), traitRecord, TRAIT_PROP, Collections.emptyList());
//				Set<String> traitNames = traitRecord.getTraits().stream()
//						.map(e -> e.getName())
//						.collect(Collectors.toSet());
//				Set<String> envVarNames = envRecord.getVariables().stream()
//						.map(e -> e.getName())
//						.collect(Collectors.toSet());
//				// result
//				callback.accept(new IndexLoaderRecord(speciesName, traitNames, envVarNames));
			}
		}
	}
	
	private void processSpeciesCounts(Map<String, Integer> speciesCounts) throws IOException {
		for (Entry<String, Integer> curr : speciesCounts.entrySet()) {
			loader.addSpecies(curr.getKey(), curr.getValue());
		}
	}

	public void setLoader(LoaderClient loader) {
		this.loader = loader;
	}
}
