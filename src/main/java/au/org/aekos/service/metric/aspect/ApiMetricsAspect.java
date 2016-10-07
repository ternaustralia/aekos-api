package au.org.aekos.service.metric.aspect;

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

import au.org.aekos.controller.ApiV1AllSpeciesRetrievalController;
import au.org.aekos.controller.ApiV1EnvVarRetrievalController;
import au.org.aekos.controller.ApiV1SearchController;
import au.org.aekos.controller.ApiV1SpeciesRetrievalController;
import au.org.aekos.controller.ApiV1TraitRetrievalController;
import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.metric.RequestRecorder;
import au.org.aekos.service.metric.RequestRecorder.RequestType;

@Aspect
@Component
public class ApiMetricsAspect {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiMetricsAspect.class);
	private static final String[] NO_DATA_FOR_THIS = new String[] {};
	private static final String AEKOS_CONTROLLER_PACKAGE = "au.org.aekos.controller.";
	private static final String V1_SEARCH_CONTROLLER_NAME = "ApiV1SearchController";
	private static final String V1_SEARCH_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_SEARCH_CONTROLLER_NAME;
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME = "ApiV1EnvVarRetrievalController";
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_SPECIES_RETRIEVAL_CONTROLLER_NAME = "ApiV1SpeciesRetrievalController";
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

	private static final String GET_ENVIRONMENT_BY_SPECIES = "getEnvironmentBySpecies";
	static final ErrorCounterName V1_GET_ENVIRONMENT_BY_SPECIES_ERRORS_COUNTER = v1SearchControllerErrors(GET_ENVIRONMENT_BY_SPECIES);

	@AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_ENVIRONMENT_BY_SPECIES + "(..))")
    public void afterCallingGetEnvironmentBySpecies(JoinPoint joinPoint) {
		recordCallWithSpeciesNamesAndSearchPaging(v1SearchControllerServices(GET_ENVIRONMENT_BY_SPECIES), RequestType.V1_ENVIRONMENT_BY_SPECIES, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_ENVIRONMENT_BY_SPECIES + "(..))", throwing = "e")
    public void afterGetEnvironmentBySpeciesThrowsException(Exception e) {
        counterService.increment(V1_GET_ENVIRONMENT_BY_SPECIES_ERRORS_COUNTER.getFullName());
    }
    
	private static final String GET_SPECIES_BY_TRAIT = "getSpeciesByTrait";
	static final ErrorCounterName V1_GET_SPECIES_BY_TRAIT_ERRORS_COUNTER = v1SearchControllerErrors(GET_SPECIES_BY_TRAIT);

    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_SPECIES_BY_TRAIT + "(..))")
    public void afterCallingGetSpeciesByTrait(JoinPoint joinPoint) {
    	recordCallWithTraitsOrEnvVarsAndSearchPaging(v1SearchControllerServices(GET_SPECIES_BY_TRAIT), RequestType.V1_SPECIES_BY_TRAIT, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_SPECIES_BY_TRAIT + "(..))", throwing = "e")
    public void afterGetSpeciesByTraitThrowsException(Exception e) {
        counterService.increment(V1_GET_SPECIES_BY_TRAIT_ERRORS_COUNTER.getFullName());
    }

	private static final String SPECIES_SUMMARY = "speciesSummary";
	static final ErrorCounterName V1_SPECIES_SUMMARY_ERRORS_COUNTER = v1SearchControllerErrors(SPECIES_SUMMARY);
	
    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + SPECIES_SUMMARY + "(..))")
    public void afterCallingGetSpeciesSummary(JoinPoint joinPoint) {
    	recordCallWithSpecies(v1SearchControllerServices(SPECIES_SUMMARY), RequestType.V1_SPECIES_SUMMARY, joinPoint);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + SPECIES_SUMMARY + "(..))", throwing = "e")
    public void afterGetSpeciesSummaryThrowsException(Exception e) {
        counterService.increment(V1_SPECIES_SUMMARY_ERRORS_COUNTER.getFullName());
    }
    
    private static final String GET_TRAITS_BY_SPECIES = "getTraitsBySpecies";
	static final ErrorCounterName V1_GET_TRAITS_BY_SPECIES_ERRORS_COUNTER = v1SearchControllerErrors(GET_TRAITS_BY_SPECIES);
    
    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_TRAITS_BY_SPECIES + "(..))")
    public void afterCallingGetTraitsBySpecies(JoinPoint joinPoint) {
    	recordCallWithSpeciesNamesAndSearchPaging(v1SearchControllerServices(GET_TRAITS_BY_SPECIES), RequestType.V1_TRAIT_BY_SPECIES, joinPoint);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_TRAITS_BY_SPECIES + "(..))", throwing = "e")
    public void afterGetTraitsBySpeciesThrowsException(Exception e) {
        counterService.increment(V1_GET_TRAITS_BY_SPECIES_ERRORS_COUNTER.getFullName());
    }

    private static final String GET_TRAIT_VOCAB = "getTraitVocab";
	static final ErrorCounterName V1_GET_TRAIT_VOCAB_ERRORS_COUNTER = v1SearchControllerErrors(GET_TRAIT_VOCAB);
    
    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_TRAIT_VOCAB + "(..))")
    public void afterCallingGetTraitVocab() {
    	recordCallWithoutParams(v1SearchControllerServices(GET_TRAIT_VOCAB), RequestType.V1_TRAIT_VOCAB);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_TRAIT_VOCAB + "(..))", throwing = "e")
    public void afterGetTraitVocabThrowsException(Exception e) {
        counterService.increment(V1_GET_TRAIT_VOCAB_ERRORS_COUNTER.getFullName());
    }
    
    private static final String GET_ENVIRONMENTAL_VARIABLE_VOCAB = "getEnvironmentalVariableVocab";
	static final ErrorCounterName V1_GET_ENVIRONMENTAL_VARIABLE_VOCAB_ERRORS_COUNTER = v1SearchControllerErrors(GET_ENVIRONMENTAL_VARIABLE_VOCAB);
    
    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_ENVIRONMENTAL_VARIABLE_VOCAB + "(..))")
    public void afterCallingGetEnvironmentalVariableVocab() {
    	recordCallWithoutParams(v1SearchControllerServices(GET_ENVIRONMENTAL_VARIABLE_VOCAB), RequestType.V1_ENVVAR_VOCAB);
    }

	@AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + GET_ENVIRONMENTAL_VARIABLE_VOCAB + "(..))", throwing = "e")
    public void afterGetEnvironmentalVariableVocabThrowsException(Exception e) {
        counterService.increment(V1_GET_ENVIRONMENTAL_VARIABLE_VOCAB_ERRORS_COUNTER.getFullName());
    }

    private static final String SPECIES_AUTOCOMPLETE = "speciesAutocomplete";
	static final ErrorCounterName V1_SPECIES_AUTOCOMPLETE_ERRORS_COUNTER = v1SearchControllerErrors(SPECIES_AUTOCOMPLETE);
    
    @AfterReturning(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + SPECIES_AUTOCOMPLETE + "(..))")
    public void afterCallingSpeciesAutocomplete(JoinPoint joinPoint) {
    	ServicesCounterName counterName = v1SearchControllerServices(SPECIES_AUTOCOMPLETE);
		counterService.increment(counterName.getFullName());
    	String speciesFragment = (String) joinPoint.getArgs()[0];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + speciesFragment);
		metricsQueue.recordRequestAutocomplete(apiAuthKey("TODO", counterName), RequestType.V1_SPECIES_AUTOCOMPLETE, speciesFragment);
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SEARCH_CONTROLLER + "." + SPECIES_AUTOCOMPLETE + "(..))", throwing = "e")
    public void afterSpeciesAutocompleteThrowsException(Exception e) {
        counterService.increment(V1_SPECIES_AUTOCOMPLETE_ERRORS_COUNTER.getFullName());
    }
    
    
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
    
	private void recordCallWithoutParams(ServicesCounterName counterName, RequestType reqType) {
		counterService.increment(counterName.getFullName());
		logMethodCall(" *** " + counterName.getMethodName() + " called..");
		metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType);
	}
	
	private void recordCallWithSpecies(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
		counterService.increment(counterName.getFullName());
    	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(speciesNames));
		metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType, speciesNames);
	}
	
	private void recordCallWithSpeciesNamesAndRetrievalPaging(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
        counterService.increment(counterName.getFullName());
    	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
    	int start = (int) joinPoint.getArgs()[1];
    	int rows = (int) joinPoint.getArgs()[2];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(speciesNames));
		metricsQueue.recordRequest(apiAuthKey("TODO", counterName), reqType, speciesNames, NO_DATA_FOR_THIS, start, rows);
	}
	
	private void recordCallWithSpeciesNamesAndSearchPaging(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
		counterService.increment(counterName.getFullName());
    	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
    	int pageNum = (int) joinPoint.getArgs()[1];
    	int pageSize = (int) joinPoint.getArgs()[2];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(speciesNames));
		metricsQueue.recordRequestWithSpecies(apiAuthKey("TODO", counterName), reqType, speciesNames, pageNum, pageSize);
	}
	
	private void recordCallWithTraitsOrEnvVarsAndSearchPaging(ServicesCounterName counterName, RequestType reqType, JoinPoint joinPoint) {
		counterService.increment(counterName.getFullName());
    	String[] traitsOrEnvVars = (String[]) joinPoint.getArgs()[0];
    	int pageNum = (int) joinPoint.getArgs()[1];
    	int pageSize = (int) joinPoint.getArgs()[2];
		logMethodCall(" *** " + counterName.getMethodName() + " called.. " + Arrays.toString(traitsOrEnvVars));
		metricsQueue.recordRequestWithTraitsOrEnvVars(apiAuthKey("TODO", counterName), reqType, traitsOrEnvVars, pageNum, pageSize);
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
	
	private static ServicesCounterName v1SearchControllerServices(String methodName) {
    	return servicesCounter(V1_SEARCH_CONTROLLER_NAME, methodName);
    }
    
    private static ErrorCounterName v1SearchControllerErrors(String methodName) {
		return errorsCounter(V1_SEARCH_CONTROLLER_NAME, methodName);
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
    
    private static ErrorCounterName errorsCounter(String controllerName, String methodName) {
    	return new ErrorCounterName(controllerName, methodName);
    }
    
    private static ServicesCounterName servicesCounter(String controllerName, String methodName) {
    	return new ServicesCounterName(controllerName, methodName);
    }
    
    private void logMethodCall(String msg) {
		logger.debug(msg);
	}

    /**
     * This class depends on these other ones and doing this leverages the compiler to help look out for problems.
     */
    static {
    	ApiV1EnvVarRetrievalController.class.toString();
		ApiV1SpeciesRetrievalController.class.toString();
		ApiV1AllSpeciesRetrievalController.class.toString();
		ApiV1TraitRetrievalController.class.toString();
		ApiV1SearchController.class.toString();
	}
}