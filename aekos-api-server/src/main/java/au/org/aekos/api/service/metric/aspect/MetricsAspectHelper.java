package au.org.aekos.api.service.metric.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.aekos.api.service.auth.AekosApiAuthKey;
import au.org.aekos.api.service.auth.AekosApiAuthKey.InvalidKeyException;

public class MetricsAspectHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(MetricsAspectHelper.class);
	static final String AEKOS_CONTROLLER_PACKAGE = "au.org.aekos.api.controller.";

	AekosApiAuthKey apiAuthKey(String keyString, ServicesCounterName counterName) {
		try {
			return new AekosApiAuthKey(keyString);
		} catch (InvalidKeyException e) {
			logger.error("afterCalling" + counterName.getMethodName() + " bad apiKey exception");
			try {
				return new AekosApiAuthKey("INVALID_KEY_WAS_SUPPLIED");
			} catch (InvalidKeyException e1) {
				throw new RuntimeException("Programmer error: a hardcoded fallback key should always be valid. "
						+ "Original exception message = " + e.getMessage(), e1);
			}
		}
	}
    
    static ErrorCounterName errorsCounter(String controllerName, String methodName) {
    	return new ErrorCounterName(controllerName, methodName);
    }
    
    static ServicesCounterName servicesCounter(String controllerName, String methodName) {
    	return new ServicesCounterName(controllerName, methodName);
    }
}