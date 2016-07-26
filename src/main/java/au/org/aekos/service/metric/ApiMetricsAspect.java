package au.org.aekos.service.metric;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import au.org.aekos.controller.ApiV1AllSpeciesRetrievalController;
import au.org.aekos.controller.ApiV1EnvVarRetrievalController;
import au.org.aekos.controller.ApiV1SpeciesRetrievalController;
import au.org.aekos.controller.ApiV1TraitRetrievalController;
import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;

@Aspect
@Component
public class ApiMetricsAspect {
	
	// FIXME handle API key exceptions centrally (they should never happen anyway)
	// FIXME extract constants from the search methods
	// FIXME extract method name constants
	
	private static final Logger logger = LoggerFactory.getLogger(ApiMetricsAspect.class);
	private static final String AEKOS_CONTROLLER_PACKAGE = "au.org.aekos.controller.";
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME = "ApiV1EnvVarRetrievalController";
	private static final String V1_ENV_VAR_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_SPECIES_RETRIEVAL_CONTROLLER_NAME = "ApiV1SpeciesRetrievalController";
	private static final String V1_SPECIES_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_SPECIES_RETRIEVAL_CONTROLLER_NAME;
	private static final String V1_TRAIT_RETRIEVAL_CONTROLLER_NAME = "ApiV1TraitRetrievalController";
	private static final String V1_TRAIT_RETRIEVAL_CONTROLLER = AEKOS_CONTROLLER_PACKAGE + V1_TRAIT_RETRIEVAL_CONTROLLER_NAME;

    private final CounterService counterService;

    @Autowired
    public ApiMetricsAspect(CounterService counterService) {
        this.counterService = counterService;
    }
    
	@Autowired
    private MetricsStorageService metricStore;

    // GetEnvironmentBySpecies
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentBySpecies(..))")
    public void afterCallingGetEnvironmentBySpecies(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.getEnvironmentBySpecies.invoked");
        
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** GetEnvironmentBySpecies called.. " + Arrays.toString(speciesNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_AUTOCOMPLETE, speciesNames);
		} catch (InvalidKeyException e) {
			logger.error("GetEnvironmentBySpecies bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentBySpecies(..))", throwing = "e")
    public void afterGetEnvironmentBySpeciesThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getEnvironmentBySpecies");
    }
    
    // GetSpeciesByTrait
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesByTrait(..))")
    public void afterCallingGetSpeciesByTrait(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.getSpeciesByTrait.invoked");
        
        try {
        	String[] traitNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** GetSpeciesByTrait called.. " + Arrays.toString(traitNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_AUTOCOMPLETE, traitNames);
		} catch (InvalidKeyException e) {
			logger.error("GetSpeciesByTrait bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesByTrait(..))", throwing = "e")
    public void afterGetSpeciesByTraitThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getSpeciesByTrait");
    }

    // GetSpeciesSummary
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesSummary(..))")
    public void afterCallingGetSpeciesSummary(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.getSpeciesSummary.invoked");
        
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** GetSpeciesSummary called.. " + Arrays.toString(speciesNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_AUTOCOMPLETE, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("GetSpeciesSummary bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesSummary(..))", throwing = "e")
    public void afterGetSpeciesSummaryThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getSpeciesSummary");
    }
    
    
    // GetTraitsBySpecies
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitsBySpecies(..))")
    public void afterCallingGetTraitsBySpecies(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.getTraitsBySpecies.invoked");
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** GetTraitsBySpecies called.. " + Arrays.toString(speciesNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_AUTOCOMPLETE, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("GetTraitsBySpecies bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitsBySpecies(..))", throwing = "e")
    public void afterGetTraitsBySpeciesThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getTraitsBySpecies");
    }

    
    
    // GetTraitVocab
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitVocab(..))")
    public void afterCallingGetTraitVocab() {
        counterService.increment("services.api.ApiV1SearchController.getTraitVocab.invoked");
        try {
			logMethodCall(" *** GetTraitVocab called..");
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_TRAIT_VOCAB);
		} catch (InvalidKeyException e) {
			logger.error("GetTraitVocab bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitVocab(..))", throwing = "e")
    public void afterGetTraitVocabThrowsException(NoSuchElementException e) {
        counterService.increment("counter.errors.ApiV1SearchController.getTraitVocab");
    }
    
    
    
    // GetEnvironmentalVariableVocab
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentalVariableVocab(..))")
    public void afterCallingGetEnvironmentalVariableVocab() {
        counterService.increment("services.api.ApiV1SearchController.getEnvironmentalVariableVocab.invoked");
		try {
			logMethodCall(" *** GetEnvironmentalVariableVocab called..");
			metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ENVVAR_VOCAB);
		} catch (InvalidKeyException e) {
			logger.error("GetEnvironmentalVariableVocab bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentalVariableVocab(..))", throwing = "e")
    public void afterGetEnvironmentalVariableVocabThrowsException(NoSuchElementException e) {
        counterService.increment("counter.errors.ApiV1SearchController.getEnvironmentalVariableVocab");
    }

    
    // SpeciesAutocomplete
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.speciesAutocomplete(..))")
    public void afterCallingSpeciesAutocomplete(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.speciesAutocomplete.invoked");
        try {
        	String speciesStub = (String) joinPoint.getArgs()[0];
			logMethodCall(" *** SpeciesAutocomplete called.. " + speciesStub);
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_AUTOCOMPLETE, new String[] { speciesStub });
		} catch (InvalidKeyException e) {
			logger.error("SpeciesAutocomplete bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.speciesAutocomplete(..))", throwing = "e")
    public void afterSpeciesAutocompleteThrowsException(NoSuchElementException e) {
        counterService.increment("counter.errors.ApiV1SearchController.speciesAutocomplete");
    }
    
    /*
     * ApiV1EnvVarRetrievalController
     */

    // environmentDataDotCsv
    @AfterReturning(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + ".environmentDataDotCsv(..))")
    public void afterCallingEnvironmentDataDotCsv(JoinPoint joinPoint) {
        counterService.increment(v1EnvVarRetrievalControllerServices("environmentDataDotCsv"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
        	String[] envNames = (String[]) joinPoint.getArgs()[1];
			logMethodCall(" *** environmentDataDotCsv called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ENVIRONMENT_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("environmentDataDotCsv bad apiKey exception");
		}
    }

	@AfterThrowing(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + ".environmentDataDotCsv(..))", throwing = "e")
    public void afterEnvironmentDataDotCsvThrowsException(Exception e) {
        counterService.increment(v1EnvVarRetrievalControllerErrors("environmentDataDotCsv"));
    }

	// environmentDataDotJson
    @AfterReturning(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + ".environmentDataDotJson(..))")
    public void afterCallingenvironmentDataDotJson(JoinPoint joinPoint) {
        counterService.increment(v1EnvVarRetrievalControllerServices("environmentDataDotJson"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
        	String[] envNames = (String[]) joinPoint.getArgs()[1];
			logMethodCall(" *** environmentDataDotJson called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ENVIRONMENT_DATA_JSON, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("environmentDataDotJson bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_ENV_VAR_RETRIEVAL_CONTROLLER + ".environmentDataDotJson(..))", throwing = "e")
    public void afterenvironmentDataDotJsonThrowsException(Exception e) {
        counterService.increment(v1EnvVarRetrievalControllerErrors("environmentDataDotJson"));
    }
    
    /*
     * ApiV1SpeciesRetrievalController
     */
    
    // speciesDataDotCsv
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".speciesDataDotCsv(..))")
    public void afterCallingSpeciesDataDotCsv(JoinPoint joinPoint) {
        counterService.increment(v1SpeciesRetrievalControllerServices("speciesDataDotCsv"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** speciesDataDotCsv called.. " + Arrays.toString(speciesNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("speciesDataDotCsv bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".speciesDataDotCsv(..))", throwing = "e")
    public void afterSpeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment(v1SpeciesRetrievalControllerErrors("speciesDataDotCsv"));
    }
    
    // speciesDataDotJson
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".speciesDataDotJson(..))")
    public void afterCallingSpeciesDataDotJson(JoinPoint joinPoint) {
        counterService.increment(v1SpeciesRetrievalControllerServices("speciesDataDotJson"));
		try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** speciesDataDotCsv called.. " + Arrays.toString(speciesNames));
			metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_DATA_JSON);
		} catch (InvalidKeyException e) {
			logger.error("afterCallingspeciesDataDotJson bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".speciesDataDotJson(..))", throwing = "e")
    public void afterSpeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment(v1SpeciesRetrievalControllerErrors("speciesDataDotJson"));
    }
    
    // allSpeciesDataDotCsv
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".allSpeciesDataDotCsv(..))")
    public void afterCallingAllSpeciesDataDotCsv(JoinPoint joinPoint) {
        counterService.increment(v1SpeciesRetrievalControllerServices("allSpeciesDataDotCsv"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** allSpeciesDataDotCsv called.. " + Arrays.toString(speciesNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ALL_SPECIES_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("speciesDataDotCsv bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".allSpeciesDataDotCsv(..))", throwing = "e")
    public void afterAllSpeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment(v1SpeciesRetrievalControllerErrors("allSpeciesDataDotCsv"));
    }
    
    // allSpeciesDataDotJson
    @AfterReturning(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".allSpeciesDataDotJson(..))")
    public void afterCallingAllSpeciesDataDotJson(JoinPoint joinPoint) {
        counterService.increment(v1SpeciesRetrievalControllerServices("allSpeciesDataDotJson"));
		try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
			logMethodCall(" *** allSpeciesDataDotJson called.. " + Arrays.toString(speciesNames));
			metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ALL_SPECIES_DATA_JSON);
		} catch (InvalidKeyException e) {
			logger.error("afterCallingspeciesDataDotJson bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_SPECIES_RETRIEVAL_CONTROLLER + ".allSpeciesDataDotJson(..))", throwing = "e")
    public void afterAllSpeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment(v1SpeciesRetrievalControllerErrors("allSpeciesDataDotJson"));
    }
    
    /*
     * ApiV1TraitRetrievalController
     */
    
    // traitDataDotCsv
    @AfterReturning(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + ".traitDataDotCsv(..))")
    public void afterCallingtraitDataDotCsv(JoinPoint joinPoint) {
        counterService.increment(v1TraitRetrievalControllerServices("traitDataDotCsv"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
        	String[] envNames = (String[]) joinPoint.getArgs()[1];
			logMethodCall(" *** traitDataDotCsv called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_TRAIT_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("traitDataDotCsv bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + ".traitDataDotCsv(..))", throwing = "e")
    public void afterTraitDataDotCsvThrowsException(Exception e) {
        counterService.increment(v1TraitRetrievalControllerErrors("traitDataDotCsv"));
    }
    
    // traitDataDotJson
    @AfterReturning(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + ".traitDataDotJson(..))")
    public void afterCallingTraitDataDotJson(JoinPoint joinPoint) {
        counterService.increment(v1TraitRetrievalControllerServices("traitDataDotJson"));
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0];
        	String[] envNames = (String[]) joinPoint.getArgs()[1];
			logMethodCall(" *** traitDataDotJson called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_TRAIT_DATA_JSON, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("traitDataDotJson bad apiKey exception");
		}
    }

	@AfterThrowing(pointcut = "execution(* " + V1_TRAIT_RETRIEVAL_CONTROLLER + ".traitDataDotJson(..))", throwing = "e")
    public void afterTraitDataDotJsonThrowsException(Exception e) {
        counterService.increment(v1TraitRetrievalControllerErrors("traitDataDotJson"));
    }
    
    private String v1EnvVarRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private String v1EnvVarRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_ENV_VAR_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private String v1SpeciesRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private String v1SpeciesRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_SPECIES_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private String v1TraitRetrievalControllerServices(String methodName) {
    	return servicesCounter(V1_TRAIT_RETRIEVAL_CONTROLLER_NAME, methodName);
    }
    
    private String v1TraitRetrievalControllerErrors(String methodName) {
		return errorsCounter(V1_TRAIT_RETRIEVAL_CONTROLLER_NAME, methodName);
	}
    
    private String errorsCounter(String controllerName, String methodName) {
    	return "counter.errors." + controllerName + "." + methodName;
    }
    
    private String servicesCounter(String controllerName, String methodName) {
    	return "services.api." + controllerName + "." + methodName + ".invoked";
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
	}
}