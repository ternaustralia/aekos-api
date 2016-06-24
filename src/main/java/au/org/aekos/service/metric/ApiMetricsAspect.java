package au.org.aekos.service.metric;

import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiMetricsAspect {

    private final CounterService counterService;

    @Autowired
    public ApiMetricsAspect(CounterService counterService) {
        this.counterService = counterService;
    }

    /*
     * Metrics on Search Services
     */

    /* 
     * ApiV1SearchController
     */

    // GetEnvironmentBySpecies
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentBySpecies(..))")
    public void afterCallingGetEnvironmentBySpecies() {
        counterService.increment("services.api.ApiV1SearchController.getEnvironmentBySpecies.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getEnvironmentBySpecies(..))", throwing = "e")
    public void afterGetEnvironmentBySpeciesThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getEnvironmentBySpecies");
    }
    

    // GetSpeciesByTrait
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesByTrait(..))")
    public void afterCallingGetSpeciesByTrait() {
        counterService.increment("services.api.ApiV1SearchController.getSpeciesByTrait.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesByTrait(..))", throwing = "e")
    public void afterGetSpeciesByTraitThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getSpeciesByTrait");
    }

    
    // GetSpeciesSummary
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesSummary(..))")
    public void afterCallingGetSpeciesSummary() {
        counterService.increment("services.api.ApiV1SearchController.getSpeciesSummary.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getSpeciesSummary(..))", throwing = "e")
    public void afterGetSpeciesSummaryThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getSpeciesSummary");
    }  
    
    
    // GetTraitsBySpecies
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitsBySpecies(..))")
    public void afterCallingGetTraitsBySpecies() {
        counterService.increment("services.api.ApiV1SearchController.getTraitsBySpecies.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitsBySpecies(..))", throwing = "e")
    public void afterGetTraitsBySpeciesThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1SearchController.getTraitsBySpecies");
    }

    
    // GetTraitVocab
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitVocab(..))")
    public void afterCallingGetTraitVocab() {
        counterService.increment("services.api.ApiV1SearchController.getTraitVocab.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.getTraitVocab(..))", throwing = "e")
    public void afterGetTraitVocabThrowsException(NoSuchElementException e) {
        counterService.increment("counter.errors.ApiV1SearchController.getTraitVocab");
    }

    
    // SpeciesAutocomplete
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1SearchController.speciesAutocomplete(..))")
    public void afterCallingSpeciesAutocomplete() {
        counterService.increment("services.api.ApiV1SearchController.speciesAutocomplete.invoked");
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
    public void afterCallingenvironmentDataDotCsv() {
        counterService.increment("services.api.ApiV1RetrievalController.environmentDataDotCsv.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotCsv(..))", throwing = "e")
    public void afterenvironmentDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.environmentDataDotCsv");
    }
    
    // environmentDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotJson(..))")
    public void afterCallingenvironmentDataDotJson() {
        counterService.increment("services.api.ApiV1RetrievalController.environmentDataDotJson.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.environmentDataDotJson(..))", throwing = "e")
    public void afterenvironmentDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.environmentDataDotJson");
    }
    
    
    
    // speciesDataDotCsv
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotCsv(..))")
    public void afterCallingspeciesDataDotCsv() {
        counterService.increment("services.api.ApiV1RetrievalController.speciesDataDotCsv.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotCsv(..))", throwing = "e")
    public void afterspeciesDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.speciesDataDotCsv");
    }
    
    // speciesDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotJson(..))")
    public void afterCallingspeciesDataDotJson() {
        counterService.increment("services.api.ApiV1RetrievalController.speciesDataDotJson.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.speciesDataDotJson(..))", throwing = "e")
    public void afterspeciesDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.speciesDataDotJson");
    }   

    
    
    
    // traitDataDotCsv
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotCsv(..))")
    public void afterCallingtraitDataDotCsv() {
        counterService.increment("services.api.ApiV1RetrievalController.traitDataDotCsv.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotCsv(..))", throwing = "e")
    public void aftertraitDataDotCsvThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.traitDataDotCsv");
    }
    
    // traitDataDotJson
    @AfterReturning(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotJson(..))")
    public void afterCallingtraitDataDotJson() {
        counterService.increment("services.api.ApiV1RetrievalController.traitDataDotJson.invoked");
    }

    @AfterThrowing(pointcut = "execution(* au.org.aekos.controller.ApiV1RetrievalController.traitDataDotJson(..))", throwing = "e")
    public void aftertraitDataDotJsonThrowsException(Exception e) {
        counterService.increment("counter.errors.ApiV1RetrievalController.traitDataDotJson");
    }   
    
}