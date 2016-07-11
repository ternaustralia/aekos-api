package au.org.aekos.service.metric;

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
	
	@Autowired
	@Qualifier("metricsModel")
	private Model metricsModel;
	
	@Autowired
	private IdProvider idProvider;
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params) {
		Resource type = metricsModel.createResource(reqType.getFullnamespace());
		Resource subject = metricsModel.createResource(idProvider.nextId(), type);
		Property authKeyProp = metricsModel.createProperty(AUTH_KEY_PROP);
		subject.addLiteral(authKeyProp, authKey.getKeyStringValue());
		params.appendTo(subject, metricsModel);
	}

	public void setMetricsModel(Model metricsModel) {
		this.metricsModel = metricsModel;
	}

	public void setIdProvider(IdProvider idProvider) {
		this.idProvider = idProvider;
	}
}
