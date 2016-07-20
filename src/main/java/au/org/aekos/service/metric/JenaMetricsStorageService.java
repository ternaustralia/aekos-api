package au.org.aekos.service.metric;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.SpeciesDataParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

@Service
public class JenaMetricsStorageService implements MetricsStorageService {
	
	public static final String METRICS_NAMESPACE_V1_0 = Application.API_NAMESPACE_V1_0 + "metrics#";
	private static final String AUTH_KEY_PROP = METRICS_NAMESPACE_V1_0 + "authKey";
	private static final String EVENT_DATE_PROP = METRICS_NAMESPACE_V1_0 + "eventDate";
	private static final String SPECIES_OR_TRAIT_OR_ENVVAR_NAMES_PROP = "paramSpeciesOrTraitOrEnvVarNames";
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
	private IdProvider idProvider;
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType) {
		recordRequestHelper(authKey, reqType);
	}

	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params) {
		Resource subject = recordRequestHelper(authKey, reqType);
		params.appendTo(subject, metricsModel);
	}
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesNames) {
		Resource subject = recordRequestHelper(authKey, reqType);
		Property speciesNamesProp = metricsModel.createProperty(SpeciesDataParams.SPECIES_NAMES_PROP);
		for (String curr : speciesNames) {
			metricsModel.add(subject, speciesNamesProp, curr);
		}
	}
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames,
			int start, int rows) {
		Resource subject = recordRequestHelper(authKey, reqType);
		Property namesProp = metricsModel.createProperty(SPECIES_OR_TRAIT_OR_ENVVAR_NAMES_PROP);
		for (String curr : speciesOrTraitOrEnvVarNames) {
			metricsModel.add(subject, namesProp, curr);
		}
		Property startProp = metricsModel.createProperty(AbstractParams.START_PROP);
		metricsModel.addLiteral(subject, startProp, start);
		Property rowsProp = metricsModel.createProperty(AbstractParams.ROWS_PROP);
		metricsModel.addLiteral(subject, rowsProp, rows);
	}

	@Override
	public Map<RequestType, Integer> getRequestSummary() {
		Map<RequestType, Integer> result = new HashMap<>();
		if (metricsModel.isEmpty()) {
			return Collections.unmodifiableMap(result);
		}
		Query query = QueryFactory.create(REQ_SUMMARY_SPARQL);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, metricsModel)) {
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

	public void setIdProvider(IdProvider idProvider) {
		this.idProvider = idProvider;
	}
}
