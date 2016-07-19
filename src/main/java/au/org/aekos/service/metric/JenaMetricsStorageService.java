package au.org.aekos.service.metric;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.Application;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

@Service
public class JenaMetricsStorageService implements MetricsStorageService {
	
	public static final String METRICS_NAMESPACE_V1_0 = Application.API_NAMESPACE_V1_0 + "metrics#";
	private static final String AUTH_KEY_PROP = METRICS_NAMESPACE_V1_0 + "authKey";
	private static final String EVENT_DATE_PROP = METRICS_NAMESPACE_V1_0 + "eventDate";
	
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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, String[] speciesOrTraitOrEnvVarNames,
			int start, int rows) {
		// TODO Auto-generated method stub
		
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
		@Override
		public long getEventDate() {
			return new GregorianCalendar(TimeZone.getTimeZone("Australia/South Australia")).getTimeInMillis();
		}
	}

	public void setMetricsModel(Model metricsModel) {
		this.metricsModel = metricsModel;
	}

	public void setIdProvider(IdProvider idProvider) {
		this.idProvider = idProvider;
	}
}
