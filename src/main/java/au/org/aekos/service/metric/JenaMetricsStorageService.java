package au.org.aekos.service.metric;

import java.io.Writer;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.Application;
import au.org.aekos.service.auth.AekosApiAuthKey;

@Service
public class JenaMetricsStorageService implements MetricsStorageService {
	
	public static final String METRICS_NAMESPACE_V1_0 = Application.API_NAMESPACE_V1_0 + "metrics#";
	private static final String AUTH_KEY_PROP = METRICS_NAMESPACE_V1_0 + "authKey";
	private static final String EVENT_DATE_PROP = METRICS_NAMESPACE_V1_0 + "eventDate";
	private static final String TRAIT_OR_ENVVAR_NAMES_PROP = METRICS_NAMESPACE_V1_0 + "paramTraitOrEnvVarNames";
	private static final String PAGE_NUM = METRICS_NAMESPACE_V1_0 + "paramPageNum";
	private static final String PAGE_SIZE = METRICS_NAMESPACE_V1_0 + "paramPageSize";
	private static final String SPECIES_NAMES_PROP = METRICS_NAMESPACE_V1_0 + "paramSpeciesNames";
	private static final String START_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramStart";
	private static final String ROWS_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramRows";
	private static final String SPECIES_AUTOCOMPLETE_FRAGMENT = METRICS_NAMESPACE_V1_0 + "fragment";
	private static final String COUNT = "count";
	private static final String REQ_TYPE = "reqType";
	private static final String REQ_SUMMARY_SPARQL =
			"SELECT ?" + REQ_TYPE + " (COUNT(*) as ?" + COUNT + ") " +
			"WHERE { ?s a ?reqType . } " +
			"GROUP BY ?reqType";
	
	private EventDateProvider eventDateProvider = new GregorianCalendarEventDateProvider();
	
	@Autowired
	@Qualifier("metricsModel")
	private Model metricsModel;
	
	@Autowired
	@Qualifier("metricsDS")
	private Dataset metricsDataset;
	
	@Autowired
	private IdProvider idProvider;
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType) {
		startTransaction();
		recordRequestHelper(authKey, reqType);
		endTransaction();
	}

	@Override
	public void recordRequestWithSpecies(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames,
			int pageNum, int pageSize) {
		startTransaction();
		Resource subject = recordRequestHelper(authKey, reqType);
		Property speciesNamesProp = metricsModel.createProperty(SPECIES_NAMES_PROP);
		for (String curr : speciesNames) {
			metricsModel.add(subject, speciesNamesProp, curr);
		}
		Property pageNumProp = metricsModel.createProperty(PAGE_NUM);
		metricsModel.addLiteral(subject, pageNumProp, pageNum);
		Property pageSizeProp = metricsModel.createProperty(PAGE_SIZE);
		metricsModel.addLiteral(subject, pageSizeProp, pageSize);
		endTransaction();
	}

	@Override
	public void recordRequestWithTraitsOrEnvVars(AekosApiAuthKey authKey, RequestType reqType,
			String[] traitOrEnvVarNames, int pageNum, int pageSize) {
		startTransaction();
		Resource subject = recordRequestHelper(authKey, reqType);
		Property traitsOrEnvVarsProp = metricsModel.createProperty(TRAIT_OR_ENVVAR_NAMES_PROP);
		for (String curr : traitOrEnvVarNames) {
			metricsModel.add(subject, traitsOrEnvVarsProp, curr);
		}
		Property pageNumProp = metricsModel.createProperty(PAGE_NUM);
		metricsModel.addLiteral(subject, pageNumProp, pageNum);
		Property pageSizeProp = metricsModel.createProperty(PAGE_SIZE);
		metricsModel.addLiteral(subject, pageSizeProp, pageSize);
		endTransaction();
	}
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames) {
		startTransaction();
		Resource subject = recordRequestHelper(authKey, reqType);
		Property speciesNamesProp = metricsModel.createProperty(SPECIES_NAMES_PROP);
		for (String curr : speciesNames) {
			metricsModel.add(subject, speciesNamesProp, curr);
		}
		endTransaction();
	}
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames,
			String[] traitOrEnvVarNames, int start, int rows) {
		startTransaction();
		Resource subject = recordRequestHelper(authKey, reqType);
		Property speciesNamesProp = metricsModel.createProperty(SPECIES_NAMES_PROP);
		for (String curr : speciesNames) {
			metricsModel.add(subject, speciesNamesProp, curr);
		}
		Property traitOrEnvVarsProp = metricsModel.createProperty(TRAIT_OR_ENVVAR_NAMES_PROP);
		for (String curr : traitOrEnvVarNames) {
			metricsModel.add(subject, traitOrEnvVarsProp, curr);
		}
		Property startProp = metricsModel.createProperty(START_PROP);
		metricsModel.addLiteral(subject, startProp, start);
		Property rowsProp = metricsModel.createProperty(ROWS_PROP);
		metricsModel.addLiteral(subject, rowsProp, rows);
		endTransaction();
	}
	
	@Override
	public void recordRequestAutocomplete(AekosApiAuthKey authKey, RequestType reqType, String speciesFragment) {
		startTransaction();
		Resource subject = recordRequestHelper(authKey, reqType);
		Property autocompleteFragmentProp = metricsModel.createProperty(SPECIES_AUTOCOMPLETE_FRAGMENT);
		metricsModel.add(subject, autocompleteFragmentProp, speciesFragment);
		endTransaction();
	}

	@Override
	public Map<RequestType, Integer> getRequestSummary() {
		Map<RequestType, Integer> result = new HashMap<>();
		if (metricsModel.isEmpty()) {
			return Collections.unmodifiableMap(result);
		}
		Query query = QueryFactory.create(REQ_SUMMARY_SPARQL);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, metricsDataset)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new RuntimeException("No results were returned in the solution for the query: " + REQ_SUMMARY_SPARQL);
			}
			for (; results.hasNext();) {
				QuerySolution soln = results.next();
				String reqType = soln.getResource(REQ_TYPE).getLocalName();
				int count = soln.getLiteral(COUNT).getInt();
				result.put(RequestType.valueOf(reqType), count);
			}
		}
		return Collections.unmodifiableMap(result);
	}
	
	@Override
	public void writeRdfDump(Writer writer) {
		metricsModel.write(writer, "TURTLE");
	}
	
	private Resource recordRequestHelper(AekosApiAuthKey authKey, RequestType reqType) {
		Resource type = metricsModel.createResource(reqType.getFullnamespace());
		Resource subject = metricsModel.createResource(idProvider.nextId(), type);
		Property authKeyProp = metricsModel.createProperty(AUTH_KEY_PROP);
		subject.addLiteral(authKeyProp, authKey.getKeyStringValue());
		Property eventDateProp = metricsModel.createProperty(EVENT_DATE_PROP);
		long now = eventDateProvider.getEventDate();
		subject.addLiteral(eventDateProp, now);
		return subject;
	}
	
	public interface EventDateProvider {
		long getEventDate();
	}
	
	private void startTransaction() {
		if (!metricsModel.supportsTransactions()) {
			return;
		}
		metricsModel.begin();
	}
	
	private void endTransaction() {
		if (!metricsModel.supportsTransactions()) {
			return;
		}
		metricsModel.commit();
	}
	
	class GregorianCalendarEventDateProvider implements EventDateProvider {
		private static final String OPERATORS_TIMEZONE = "Australia/South Australia";

		@Override
		public long getEventDate() {
			return new GregorianCalendar(TimeZone.getTimeZone(OPERATORS_TIMEZONE)).getTimeInMillis();
		}
	}

	public void setMetricsModel(Model metricsModel) {
		this.metricsModel = metricsModel;
	}

	public void setMetricsDataset(Dataset metricsDataset) {
		this.metricsDataset = metricsDataset;
	}

	public void setIdProvider(IdProvider idProvider) {
		this.idProvider = idProvider;
	}
}
