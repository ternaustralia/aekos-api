package au.org.aekos.service.index;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
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
	private static final String API_NS = "http://www.aekos.org.au/api/1.0#"; // FIXME make configurable and inline into SPARQL query
	private static final String DARWIN_CORE_RECORD_TYPE = API_NS + "DarwinCoreRecord";
	
	@Autowired
	@Qualifier("coreDS")
	private Dataset ds;
	
	@Autowired
	private LoaderClient loader;

	@Autowired
	@Qualifier("darwinCoreAndTraitsQuery")
	private String darwinCoreAndTraitsQuery;
	
	private int processed = 0;
	private long lastCheckpoint;
	private long checkpointSize = 10000;
	private Model helperModel;
	
	@Override
	public String doIndexing() throws IOException {
//		int totalRecordCount = retrievalService.getTotalSpeciesRecordsHeld();
		int totalRecordCount = 1000; // FIXME
		Map<String, Integer> speciesCounts = new HashMap<>();
		loader.beginLoad();
		logger.info("Clearing existing index...");
		loader.deleteAll();
		ProgressTracker tracker = new ProgressTracker(totalRecordCount);
		getIndexStream(new IndexLoaderCallback() {
			@Override
			public void accept(IndexLoaderRecord record) {
				try {
					loader.addSpeciesTraitTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getTraitNames()));
					loader.addSpeciesEnvironmentTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
					loader.addSpeciesRecord(record);
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
		// TODO also get env data
		String sparql = darwinCoreAndTraitsQuery;
//		logger.debug("Index loader SPARQL: " + sparql);
		Query query = QueryFactory.create(sparql);
		long now = now();
		lastCheckpoint = now;
		Set<String> seenSubjects = new HashSet<>();
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			Iterator<Triple> results = qexec.execConstructTriples();
			if (!results.hasNext()) {
				throw new IllegalStateException("Data problem: no results were found. "
						+ "Do you have RDF AEKOS data loaded?");
			}
			Model model = newModel();
			int modelsProcessed = 0;
			Triple firstRecord = results.next();
			String firstRecordPredicateName = firstRecord.getPredicate().getURI();
			if (!firstRecordPredicateName.equals(RDF.type.getURI())) {
				throw new RuntimeException("Programmer problem: expected that the first record will be rdf:type but it wasn't. It was '"
						+ firstRecordPredicateName + "'");
			}
			String currentlyProcessingSubject = firstRecord.getSubject().getLocalName();
			seenSubjects.add(currentlyProcessingSubject);
			for (; results.hasNext();) {
				Triple currTriple = results.next();
				trackProgress();
				String tripleSubject = currTriple.getSubject().getLocalName();
				boolean isNewSolutionRow = currTriple.getPredicate().getURI().equals(RDF.type.getURI());
				if (isNewSolutionRow) {
					boolean isDwcRecord = currTriple.getObject().getURI().equals(DARWIN_CORE_RECORD_TYPE);
					boolean isSubjectChanged = !currentlyProcessingSubject.equals(tripleSubject);
					boolean isSubjectAlreadySeen = seenSubjects.contains(tripleSubject);
					if (isDwcRecord && isSubjectChanged) {
						if (isSubjectAlreadySeen) {
							throw new RuntimeException("FAIL town, we've already seen " + tripleSubject);
						}
						if (++modelsProcessed == 5) {
							break;
						}
						process(model, modelsProcessed, callback);
						model = newModel();
						currentlyProcessingSubject = tripleSubject;
						seenSubjects.add(currentlyProcessingSubject);
					}
				}
				model.getGraph().add(currTriple);
			}
		}
	}

	private void process(Model model, int counter, IndexLoaderCallback callback) {
		try {
			String path = "/data/model" + counter + ".ttl";
			logger.info("Writing " + path);
			FileOutputStream out = new FileOutputStream(path);
			model.write(out, "TURTLE");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("failed", e);
		}
		// FIXME check there's exactly one record
		Resource dwcRecord = model.listSubjectsWithProperty(RDF.type, model.createResource(DARWIN_CORE_RECORD_TYPE)).next();
		String speciesName = getString(dwcRecord, "scientificName");
		// env
		Set<String> envVarNames = Collections.emptySet();
//		DummyEnvironmentDataRecord envRecord = new DummyEnvironmentDataRecord();
//		for (Property currVarProp : Arrays.asList(prop(DISTURBANCE_EVIDENCE_VARS), prop(LANDSCAPE_VARS), prop(NO_UNITS_VARS), prop(SOIL_VARS))) {
//			processEnvDataVars(Collections.emptyList(), s.get("loc").asResource(), envRecord, currVarProp);
//		}
		// traits
		Set<String> traitNames = dwcRecord.listProperties(prop("trait")).toList().stream()
			.map(e -> e.getObject().asResource().getProperty(prop("name")).getLiteral().getString())
			.collect(Collectors.toSet());
		// result
		callback.accept(new IndexLoaderRecord(speciesName, traitNames, envVarNames));
	}

	private String getString(Resource res, String predicateName) {
		Property p = prop(predicateName);
		return res.getProperty(p).getLiteral().getString();
	}
	
	private Property prop(String predicateName) {
		if (helperModel == null) {
			helperModel = ModelFactory.createMemModelMaker().createFreshModel();
		}
		return helperModel.createProperty(API_NS + predicateName);
	}

	private Model newModel() {
		logger.info("new model");
		return ModelFactory.createDefaultModel();
	}

	private void trackProgress() {
		processed++;
		long now = now();
		if (now >= (lastCheckpoint + checkpointSize)) {
			lastCheckpoint = now;
			logger.info(String.format("Processed %d records", processed));
		}
	}

	private long now() {
		return new Date().getTime();
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
