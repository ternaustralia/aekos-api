package au.org.aekos.api.service.metric.aspect;

import static au.org.aekos.api.service.metric.aspect.MetricsAspectHelper.AEKOS_CONTROLLER_PACKAGE;
import static au.org.aekos.api.service.metric.aspect.MetricsAspectHelper.errorsCounter;
import static au.org.aekos.api.service.metric.aspect.MetricsAspectHelper.servicesCounter;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import au.org.aekos.api.controller.ApiV1AllSpeciesRetrievalController;
import au.org.aekos.api.controller.ApiV1EnvVarRetrievalController;
import au.org.aekos.api.controller.ApiV1TraitRetrievalController;
import au.org.aekos.api.controller.ApiV1_0SpeciesRetrievalController;
import au.org.aekos.api.controller.ApiV1_1SpeciesRetrievalController;
import au.org.aekos.api.service.auth.AekosApiAuthKey;
import au.org.aekos.api.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.api.service.metric.RequestRecorder;
import au.org.aekos.api.service.metric.RequestRecorder.RequestType;

@Aspect
@Component
public class RetrievalMetricsAspect {
	
	private static final Logger logger = LoggerFactory.getLogger(RetrievalMetricsAspect.class);
	private static final String[] NO_DATA_FOR_THIS = new String[] {};
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME = "ApiV1EnvVarRetrievalController";
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_SPECIES_RETRIEVAL_CONTROLLER_NAME = "ApiV1_0SpeciesRetrievalController";
	private static final String V1_SPECIES_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_SPECIES_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_TRAIT_RETRIEVAL_CONTROLLER_NAME = "ApiV1TraitRetrievalController";
	private static final String V1_TRAIT_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_TRAIT_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_ALL_SPECIES_RETRIEVAL_CONTROLLER_NAME = "ApiV1AllSpeciesRetrievalController";
	private static final String V1_ALL_SPECIES_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER_NAME;

	@Autowired
    private CounterService counterService;

	@Autowired
	@Qualifier("metricsQueue")
	private RequestRecorder metricsQueue;
	
	// FIXME add v1.1 coverage
    
    private static final String ENVIRONMENT_DATA_DOT_CSV = "environmentDataDotCsv";
	private static final String ENVIRONMENT_DATA_CSV = "environmentDataCsv";
	static final ErrorCounterName V1_ENVIRONMENT_DATA_DOT_CSV_ERRORS_COUNTER = v1EnvVarRetrievalControllerErrors(ENVIRONMENT_DATA_DOT_CSV);
    
    @AfterReturning(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_CSV + "(..))")
    public void afterCallingEnvironmentDataDotCsv(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndTraitsOrEnvVarsAndPaging(v1EnvVarRetrievalControllerServices(ENVIRONMENT_DATA_DOT_CSV), RequestType.V1_ENVIRONMENT_DATA_CSV, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_DOT_CSV + "(..))"
			+ " || execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_CSV + "(..))", throwing = "e")
    public void afterEnvironmentDataDotCsvThrowsException(Exception e) {
        counterService.increment(V1_ENVIRONMENT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName());
    }

	private static final String ENVIRONMENT_DATA_DOT_JSON = "environmentDataDotJson";
	private static final String ENVIRONMENT_DATA_JSON = "environmentDataJson";
	static final ErrorCounterName V1_ENVIRONMENT_DATA_DOT_JSON_ERRORS_COUNTER = v1EnvVarRetrievalControllerErrors(ENVIRONMENT_DATA_DOT_JSON);
	
    @AfterReturning(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_DOT_JSON + "(..))"
    		+ " || execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_JSON + "(..))")
    public void afterCallingenvironmentDataDotJson(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndTraitsOrEnvVarsAndPaging(v1EnvVarRetrievalControllerServices(ENVIRONMENT_DATA_DOT_JSON), RequestType.V1_ENVIRONMENT_DATA_JSON, joinPoint);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + ".environmentDataDotJson(..))"
    		+ " || execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + "." + ENVIRONMENT_DATA_JSON + "(..))", throwing = "e")
    public void afterenvironmentDataDotJsonThrowsException(Exception e) {
        counterService.increment(V1_ENVIRONMENT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName());
    }
    
    private static final String SPECIES_DATA_DOT_CSV = "speciesDataDotCsv";
	private static final String SPECIES_DATA_CSV = "speciesDataCsv";
	static final ErrorCounterName V1_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER = v1SpeciesRetrievalControllerErrors(SPECIES_DATA_DOT_CSV);
    
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_CSV + "(..))")
    public void afterCallingSpeciesDataDotCsv(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndRetrievalPaging(v1SpeciesRetrievalControllerServices(SPECIES_DATA_DOT_CSV), RequestType.V1_SPECIES_DATA_CSV, joinPoint);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_CSV + "(..))", throwing = "e")
    public void afterSpeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment(V1_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName());
    }
    
    private static final String SPECIES_DATA_DOT_JSON = "speciesDataDotJson";
	private static final String SPECIES_DATA_JSON = "speciesDataJson";
	static final ErrorCounterName V1_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER = v1SpeciesRetrievalControllerErrors(SPECIES_DATA_DOT_JSON);
    
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_DOT_JSON + "(..))"
    		+ " || execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_JSON + "(..))")
    public void afterCallingSpeciesDataDotJson(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndRetrievalPaging(v1SpeciesRetrievalControllerServices(SPECIES_DATA_DOT_JSON), RequestType.V1_SPECIES_DATA_JSON, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_DOT_JSON + "(..))"
			+ " || execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + "." + SPECIES_DATA_JSON + "(..))", throwing = "e")
    public void afterSpeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment(V1_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName());
    }
    
	private static final String ALL_SPECIES_DATA_DOT_CSV = "allSpeciesDataDotCsv";
	private static final String ALL_SPECIES_DATA_CSV = "allSpeciesDataCsv";
	static final ErrorCounterName V1_ALL_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER = v1AllSpeciesRetrievalControllerErrors(ALL_SPECIES_DATA_DOT_CSV);
	
    @AfterReturning(pointcut = "execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_CSV + "(..))")
    public void afterCallingAllSpeciesDataDotCsv(JoinPoint joinPoint) {
    	recordAllSpeciesCall(v1AllSpeciesRetrievalControllerServices(ALL_SPECIES_DATA_DOT_CSV), RequestType.V1_ALL_SPECIES_DATA_CSV, joinPoint);
    }
    
	@AfterThrowing(pointcut = "execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_DOT_CSV + "(..))"
			+ " || execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_CSV + "(..))", throwing = "e")
    public void afterAllSpeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment(V1_ALL_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName());
    }
    
    private static final String ALL_SPECIES_DATA_DOT_JSON = "allSpeciesDataDotJson";
    private static final String ALL_SPECIES_DATA_JSON = "allSpeciesDataJson";
	static final ErrorCounterName V1_ALL_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER = v1AllSpeciesRetrievalControllerErrors(ALL_SPECIES_DATA_DOT_JSON);
    
    @AfterReturning(pointcut = "execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_DOT_JSON + "(..))"
    		+ " || execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_JSON + "(..))")
    public void afterCallingAllSpeciesDataDotJson(JoinPoint joinPoint) {
    	recordAllSpeciesCall(v1AllSpeciesRetrievalControllerServices(ALL_SPECIES_DATA_DOT_JSON), RequestType.V1_ALL_SPECIES_DATA_JSON, joinPoint);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_DOT_JSON + "(..))"
    		+ " || execution(* " + V1_ALL_SPECIES_RETRIEVAL_CONTROLLER + "." + ALL_SPECIES_DATA_JSON + "(..))", throwing = "e")
    public void afterAllSpeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment(V1_ALL_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName());
    }
    
	private static final String TRAIT_DATA_DOT_CSV = "traitDataDotCsv";
	private static final String TRAIT_DATA_CSV = "traitDataCsv";
	static final ErrorCounterName V1_TRAIT_DATA_DOT_CSV_ERRORS_COUNTER = v1TraitRetrievalControllerErrors(TRAIT_DATA_DOT_CSV);
    
    @AfterReturning(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_CSV + "(..))")
    public void afterCallingtraitDataDotCsv(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndTraitsOrEnvVarsAndPaging(v1TraitRetrievalControllerServices(TRAIT_DATA_DOT_CSV), RequestType.V1_TRAIT_DATA_CSV, joinPoint);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_DOT_CSV + "(..))"
    		+ " || execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_CSV + "(..))", throwing = "e")
    public void afterTraitDataDotCsvThrowsException(Exception e) {
        counterService.increment(V1_TRAIT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName());
    }
    
    private static final String TRAIT_DATA_DOT_JSON = "traitDataDotJson";
	private static final String TRAIT_DATA_JSON = "traitDataJson";
	static final ErrorCounterName V1_TRAIT_DATA_DOT_JSON_ERRORS_COUNTER = v1TraitRetrievalControllerErrors(TRAIT_DATA_DOT_JSON);
    
    @AfterReturning(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_DOT_JSON + "(..))"
    		+ " || execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_JSON + "(..))")
    public void afterCallingTraitDataDotJson(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndTraitsOrEnvVarsAndPaging(v1TraitRetrievalControllerServices(TRAIT_DATA_DOT_JSON), RequestType.V1_TRAIT_DATA_JSON, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_DOT_JSON + "(..))"
			+ " || execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + "." + TRAIT_DATA_JSON + "(..))", throwing = "e")
    public void afterTraitDataDotJsonThrowsException(Exception e) {
        counterService.increment(V1_TRAIT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName());
    }
    
	private void recordCallWithSpeciesNamesAndRetrievalPaging(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
        counterService.increment(counterName.getFullName());
    	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
    	int start = (int) joinPoint.getArgs()[1];
    	int rows = (int) joinPoint.getArgs()[2];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(speciesNames));
		metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType, speciesNames, NO_DATA_FOR_THIS, start, rows);
	}
	
	private void recordCallWithSpeciesNamesAndTraitsOrEnvVarsAndPaging(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
		counterService.increment(counterName.getFullName());
    	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
    	String[] traitsOrEnvVars = (String[]) joinPoint.getArgs()[1];
    	int start = (int) joinPoint.getArgs()[2];
    	int rows = (int) joinPoint.getArgs()[3];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(traitsOrEnvVars));
		metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType, speciesNames, traitsOrEnvVars, start, rows);
	}
	
	private AekosApiAuthKey apiAuthKey(String keyString, ServicesCounterName counterName) {
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

	private void recordAllSpeciesCall(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
		counterService.increment(counterName.getFullName());
    	int start = (int) joinPoint.getArgs()[0];
    	int rows = (int) joinPoint.getArgs()[1];
		logMethodCall(" *** " + counterName.getMethodName() + " called..");
    	metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType, NO_DATA_FOR_THIS, NO_DATA_FOR_THIS, start, rows);
	}
	
    private static ServicesCounterName v1EnvVarRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private static ErrorCounterName v1EnvVarRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private static ServicesCounterName v1SpeciesRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private static ErrorCounterName v1SpeciesRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private static ServicesCounterName v1TraitRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_TRAIT_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private static ErrorCounterName v1TraitRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_TRAIT_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private static ServicesCounterName v1AllSpeciesRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_ALL_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private static ErrorCounterName v1AllSpeciesRetrievalControllerErrors(String methodName) {
    	return errorsCounter(V1_ALL_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private void logMethodCall(String msg) {
		logger.debug(msg);
	}

    /**
     * This class depends on these other ones and doing this leverages the compiler to help look out for problems.
     */
    static {
    	ApiV1EnvVarRetrievalController.class.toString();
		ApiV1_0SpeciesRetrievalController.class.toString();
		ApiV1_1SpeciesRetrievalController.class.toString();
		ApiV1AllSpeciesRetrievalController.class.toString();
		ApiV1TraitRetrievalController.class.toString();
	}
}