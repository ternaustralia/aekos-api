package au.org.aekos.service.metric;

import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.auth.AuthStorageService;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;

@Aspect
@Component
public class ApiMetricsAspect {


    /* 
     * ApiMetricsAspect
     */
	
	
	private static final Logger logger = LoggerFactory.getLogger(ApiMetricsAspect.class);

    private final CounterService counterService;

    @Autowired
    public ApiMetricsAspect(CounterService counterService) {
        this.counterService = counterService;
    }
    
    @Autowired
    private JenaMetricsStorageService metricStore;

    //@Autowired
    //private AuthStorageService authStore;

    /*
     * Metrics on Search Services
     */
    
    
	@PostConstruct
    private void initMetricStore() {
		Model metricsModel = ModelFactory.createDefaultModel();
		metricStore.setMetricsModel(metricsModel);
		metricStore.setIdProvider(new UuidIdProvider());
    }
    	

	
    // GetEnvironmentBySpecies
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentBySpecies(..))")
    public void afterCallingGetEnvironmentBySpecies(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1SearchController.getEnvironmentBySpecies.invoked");
        
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
			logger.info(" *** GetEnvironmentBySpecies called.. " + Arrays.toString(speciesNames));
			
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
			logger.info(" *** GetSpeciesByTrait called.. " + Arrays.toString(traitNames));
			
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
			logger.info(" *** GetSpeciesSummary called.. " + Arrays.toString(speciesNames));
			
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
			logger.info(" *** GetTraitsBySpecies called.. " + Arrays.toString(speciesNames));
			
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
			logger.info(" *** GetTraitVocab called..");
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
			logger.info(" *** GetEnvironmentalVariableVocab called..");
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
			logger.info(" *** SpeciesAutocomplete called.. " + speciesStub);
			
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
     * ApiV1RetrievalController
     */

    // environmentDataDotCsv
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotCsv(..))")
    public void afterCallingEnvironmentDataDotCsv(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.environmentDataDotCsv.invoked");

        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
        	String[] envNames = (String[]) joinPoint.getArgs()[1]; 
			logger.info(" *** environmentDataDotCsv called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ENVIRONMENT_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("environmentDataDotCsv bad apiKey exception");
		} 
    
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotCsv(..))", throwing = "e")
    public void afterEnvironmentDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.environmentDataDotCsv");
    }
    
    // environmentDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotJson(..))")
    public void afterCallingenvironmentDataDotJson(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.environmentDataDotJson.invoked");
        
        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
        	String[] envNames = (String[]) joinPoint.getArgs()[1]; 
			logger.info(" *** environmentDataDotJson called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_ENVIRONMENT_DATA_JSON, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("environmentDataDotJson bad apiKey exception");
		} 
    
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotJson(..))", throwing = "e")
    public void afterenvironmentDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.environmentDataDotJson");
    }
    
    
    
    // speciesDataDotCsv
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotCsv(..))")
    public void afterCallingSpeciesDataDotCsv(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.speciesDataDotCsv.invoked");

        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
			logger.info(" *** speciesDataDotCsv called.. " + Arrays.toString(speciesNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("speciesDataDotCsv bad apiKey exception");
		} 
    
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotCsv(..))", throwing = "e")
    public void afterSpeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.speciesDataDotCsv");
    }
    
    // speciesDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotJson(..))")
    public void afterCallingSpeciesDataDotJson(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.speciesDataDotJson.invoked");
		try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
			logger.info(" *** speciesDataDotCsv called.. " + Arrays.toString(speciesNames));

			metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_SPECIES_DATA_JSON);
		} catch (InvalidKeyException e) {
			logger.error("afterCallingspeciesDataDotJson bad apiKey exception");
		}
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotJson(..))", throwing = "e")
    public void afterSpeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.speciesDataDotJson");
    }   

    
    
    
    // traitDataDotCsv
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotCsv(..))")
    public void afterCallingtraitDataDotCsv(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.traitDataDotCsv.invoked");

        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
        	String[] envNames = (String[]) joinPoint.getArgs()[1]; 
			logger.info(" *** traitDataDotCsv called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_TRAIT_DATA_CSV, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("traitDataDotCsv bad apiKey exception");
		}     
    
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotCsv(..))", throwing = "e")
    public void afterTraitDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.traitDataDotCsv");
    }
    
    // traitDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotJson(..))")
    public void afterCallingTraitDataDotJson(JoinPoint joinPoint) {
        counterService.increment("services.api.ApiV1RetrievalController.traitDataDotJson.invoked");

        try {
        	String[] speciesNames = (String[]) joinPoint.getArgs()[0]; 
        	String[] envNames = (String[]) joinPoint.getArgs()[1]; 
			logger.info(" *** traitDataDotJson called.. " + Arrays.toString(speciesNames) + "," + Arrays.toString(envNames));
			
        	metricStore.recordRequest(new AekosApiAuthKey("ddd"), RequestType.V1_TRAIT_DATA_JSON, speciesNames );
		} catch (InvalidKeyException e) {
			logger.error("traitDataDotJson bad apiKey exception");
		}     
    
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotJson(..))", throwing = "e")
    public void afterTraitDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.traitDataDotJson");
    }   
    
}