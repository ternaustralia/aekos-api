package au.org.aekos.api.loader.service.load;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.api.loader.util.FieldNames;
import au.org.aekos.api.loader.util.ProgressTracker;

@Service
public class LuceneIndexingService implements IndexingService {

	private static final Logger logger = LoggerFactory.getLogger(LuceneIndexingService.class);
	static final String API_NS = "http://www.aekos.org.au/api/1.0#"; // FIXME make configurable and inline into SPARQL query
	private static final String DARWIN_CORE_RECORD_TYPE = API_NS + "DarwinCoreRecord";
	private static final String LOCATION_VISIT_TYPE = API_NS + "LocationVisit";
	
	@Autowired
	@Qualifier("coreDS")
	private Dataset ds;
	
	@Autowired
	private LoaderClient loader;

	@Autowired
	@Qualifier("citationDetailsQuery")
	private String citationDetailsQuery;
	
	@Autowired
	@Qualifier("darwinCoreAndTraitsQuery")
	private String darwinCoreAndTraitsQuery;
	
	@Autowired
	@Qualifier("environmentalVariablesQuery")
	private String environmentalVariablesQuery;
	
	private int processed = 0;
	private long lastCheckpoint;
	private long checkpointSize = 10000;
	private Model helperModel;
	private final Map<String, String> citationRecords = new HashMap<>();
	
	@Override
	public String doIndexing() throws IOException {
//		int totalRecordCount = retrievalService.getTotalSpeciesRecordsHeld();
		int totalRecordCount = 1000; // FIXME
		loader.beginLoad();
		logger.info("Clearing existing index...");
		loader.deleteAll();
		logger.info("Phase 1: collecting citation details");
		collectCitationDetails();
		logger.info("Phase 2: indexing darwin core records");
		ProgressTracker tracker = new ProgressTracker(totalRecordCount);
		Map<String, Integer> speciesCounts = indexSpeciesRecords(tracker);
		processSpeciesCounts(speciesCounts);
		logger.info("Phase 3: indexing location visit records");
		indexEnvironmentRecords();
		loader.endLoad();
		return tracker.getFinishedMessage();
	}

	private void collectCitationDetails() {
		String sparql = citationDetailsQuery;
		Query query = QueryFactory.create(sparql);
		long start = now();
		int processedCitationRecords = 0;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new IllegalStateException("Data problem: no results were found. "
						+ "Do you have RDF AEKOS data loaded?");
			}
			for (; results.hasNext();) {
				QuerySolution currSolution = results.next();
				try {
					String samplingProtocol = currSolution.getLiteral("samplingProtocol").getString();
					String bibliographicCitation = currSolution.getLiteral("bibliographicCitation").getString();
					citationRecords.put(samplingProtocol, bibliographicCitation);
					processedCitationRecords++;
				} catch (NullPointerException e) {
					Iterable<String> iterable = () -> currSolution.varNames();
					Set<String> vars = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toSet());
					throw new RuntimeException("Available vars: " + vars);
				}
			}
		}
		long elapsed = (now() - start) / 1000;
		logger.info(String.format("Processed %d citation records in %d seconds", processedCitationRecords, elapsed));
	}

	interface IndexLoaderCallback {
		void accept(SpeciesLoaderRecord record);
	}
	
	private Map<String, Integer> indexSpeciesRecords(ProgressTracker tracker) {
		Map<String, Integer> speciesCounts = new HashMap<>();
		getSpeciesIndexStream(new IndexLoaderCallback() {
			@Override
			public void accept(SpeciesLoaderRecord record) {
				try {
					loader.addSpeciesTraitTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getTraitNames()));
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
		return speciesCounts;
	}
	
	private void getSpeciesIndexStream(IndexLoaderCallback callback) {
		String sparql = darwinCoreAndTraitsQuery;
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
			Triple firstRecord = results.next();
			model.getGraph().add(firstRecord);
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
				boolean isPossiblyNewSolutionRow = currTriple.getPredicate().getURI().equals(RDF.type.getURI());
				if (isPossiblyNewSolutionRow) {
					boolean isDwcRecord = currTriple.getObject().getURI().equals(DARWIN_CORE_RECORD_TYPE);
					boolean isSubjectChanged = !currentlyProcessingSubject.equals(tripleSubject);
					boolean isSubjectAlreadySeen = seenSubjects.contains(tripleSubject);
					if (isDwcRecord && isSubjectChanged) {
						if (isSubjectAlreadySeen) {
							throw new RuntimeException("FAIL town, we've already seen " + tripleSubject);
						}
						processSpecies(model, callback);
						model = newModel();
						currentlyProcessingSubject = tripleSubject;
						seenSubjects.add(currentlyProcessingSubject);
					}
				}
				model.getGraph().add(currTriple);
			}
		}
	}

	private void processSpecies(Model model, IndexLoaderCallback callback) {
		List<Resource> dwcResources = model.listSubjectsWithProperty(RDF.type, model.createResource(DARWIN_CORE_RECORD_TYPE)).toList();
		assertExactlyOneResource(model, dwcResources, RecordType.DWC);
		Resource dwcResource = dwcResources.get(0);
		String speciesName = getString(dwcResource, "scientificName");
		String samplingProtocol = getString(dwcResource, "samplingProtocol");
		Set<String> traitNames = dwcResource.listProperties(prop("trait")).toList().stream()
			.map(e -> e.getObject().asResource().getProperty(prop("name")).getLiteral().getString())
			.collect(Collectors.toSet());
		String bibliographicCitation = citationRecords.get(samplingProtocol);
		if (bibliographicCitation == null) {
			String template = "Data problem: couldn't find a citation for the sampling protocol '%s'";
			throw new RuntimeException(String.format(template, samplingProtocol));
		}
		callback.accept(new SpeciesLoaderRecord(speciesName, traitNames, samplingProtocol, bibliographicCitation));
	}

	private enum RecordType {
		DWC("DarwinCoreRecord"),
		ENV("EnvironmentalVariable");
		
		private final String title;
		
		private RecordType(String title) {
			this.title = title;
		}
	}
	
	private void assertExactlyOneResource(Model model, List<Resource> resources, RecordType type) {
		if (resources.size() != 1) {
			String msgPart1 = "Data problem: expected exactly one " + type.title + " record per solution row but got %d records.";
			try {
				String msg = msgPart1 + " Wrote full model to %s";
				Path tempFilePath = Files.createTempFile("aekos" + type.title, "model.ttl");
				model.write(new FileOutputStream(tempFilePath.toFile()), "TURTLE");
				throw new IllegalStateException(String.format(msg, resources.size(), tempFilePath));
			} catch (IOException e) {
				String msg = msgPart1 + " Tried to write model to temp file for debugging but failed with reason '%s'";
				throw new IllegalStateException(String.format(msg, resources.size(), e.getMessage()));
			}
		}
	}
	
	private void indexEnvironmentRecords() {
		getEnvironmentIndexStream(new EnvironmentLoaderCallback() {
			@Override
			public void accept(EnvironmentLoaderRecord record) {
				try {
//					loader.addSpeciesEnvironmentTermsToIndex(record.getSpeciesName(), new LinkedList<>(record.getEnvironmentalVariableNames()));
					loader.addEnvRecord(record);
				} catch (IOException e) {
					throw new RuntimeException("Failed to add a record to the index: " + record.toString(), e);
				}
			}
		});
	}
	
	interface EnvironmentLoaderCallback {
		void accept(EnvironmentLoaderRecord record);
	}
	
	private void getEnvironmentIndexStream(EnvironmentLoaderCallback callback) {
		String sparql = environmentalVariablesQuery;
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
			Triple firstRecord = results.next();
			model.getGraph().add(firstRecord);
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
				boolean isPossiblyNewSolutionRow = currTriple.getPredicate().getURI().equals(RDF.type.getURI());
				if (isPossiblyNewSolutionRow) {
					boolean isLocationVisitRecord = currTriple.getObject().getURI().equals(LOCATION_VISIT_TYPE);
					boolean isSubjectChanged = !currentlyProcessingSubject.equals(tripleSubject);
					boolean isSubjectAlreadySeen = seenSubjects.contains(tripleSubject);
					if (isLocationVisitRecord && isSubjectChanged) {
						if (isSubjectAlreadySeen) {
							throw new RuntimeException("FAIL town, we've already seen " + tripleSubject);
						}
						processEnv(model, callback);
						model = newModel();
						currentlyProcessingSubject = tripleSubject;
						seenSubjects.add(currentlyProcessingSubject);
					}
				}
				model.getGraph().add(currTriple);
			}
		}
	}

	private void processEnv(Model model, EnvironmentLoaderCallback callback) {
		List<Resource> locVisitResources = model.listSubjectsWithProperty(RDF.type, model.createResource(LOCATION_VISIT_TYPE)).toList();
		assertExactlyOneResource(model, locVisitResources, RecordType.ENV);
		Resource locVisitResource = locVisitResources.get(0);
		String locationId = getString(locVisitResource, "locationID"); // FIXME field name constant
//		DummyEnvironmentDataRecord envRecord = new DummyEnvironmentDataRecord();
//		for (Property currVarProp : Arrays.asList(prop(DISTURBANCE_EVIDENCE_VARS), prop(LANDSCAPE_VARS), prop(NO_UNITS_VARS), prop(SOIL_VARS))) {
//			processEnvDataVars(Collections.emptyList(), s.get("loc").asResource(), envRecord, currVarProp);
//		}
		Set<String> envVarNames = locVisitResource.listProperties(prop(FieldNames.NO_UNITS_VARS)).toList().stream() // FIXME need to do loop like above
			.map(e -> e.getObject().asResource().getProperty(prop("name")).getLiteral().getString()) // FIXME extract field name "name" as constant
			.collect(Collectors.toSet());
		callback.accept(new EnvironmentLoaderRecord(locationId, envVarNames));
	}

	String getString(Resource res, String predicateName) {
		Property p = prop(predicateName);
		Statement property = res.getProperty(p);
		if (property == null) {
			Set<String> props = res.listProperties().toList().stream()
				.map(e -> e.getPredicate().getLocalName())
				.filter(val -> !RDF.type.getLocalName().equals(val))
				.collect(Collectors.toSet());
			String msg = String.format("Programmer or data problem: couldn't find the predicate '%s' on resource '%s', did find: %s",
					predicateName, res.getURI(), props.toString());
			throw new IllegalStateException(msg);
		}
		return property.getLiteral().getString();
	}
	
	private Property prop(String predicateName) {
		if (helperModel == null) {
			helperModel = ModelFactory.createMemModelMaker().createFreshModel();
		}
		return helperModel.createProperty(API_NS + predicateName);
	}

	private Model newModel() {
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

	public void setDs(Dataset ds) {
		this.ds = ds;
	}
}
