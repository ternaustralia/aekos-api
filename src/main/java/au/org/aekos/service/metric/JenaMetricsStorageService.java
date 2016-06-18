package au.org.aekos.service.metric;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.service.auth.AekosApiAuthKey;

@Service
public class JenaMetricsStorageService implements MetricsStorageService {
	
	private static final String METRICS_NAMESPACE = "http://www.aekos.org.au/api/1.0/metrics#";
	private static final String AUTH_KEY_PROP = METRICS_NAMESPACE + "authKey";
	private static final String REQ_TYPE_PROP = METRICS_NAMESPACE + "requestType";
	
	@Autowired
	@Qualifier("metricsModel")
	private Model metricsModel;
	
	@Override
	public void recordRequest(AekosApiAuthKey authKey, RequestType reqType, AbstractParams params) {
		Resource subject = metricsModel.createResource();
		Property authKeyProp = metricsModel.createProperty(AUTH_KEY_PROP);
		subject.addLiteral(authKeyProp, authKey.getKeyStringValue());
		Property reqTypeProp = metricsModel.createProperty(REQ_TYPE_PROP);
		subject.addLiteral(reqTypeProp, reqType.toString());
		// TODO add all props and get param to append itself
		// TODO are anonymous entities safe to use?
	}

	public void setMetricsModel(Model metricsModel) {
		this.metricsModel = metricsModel;
	}
}
