package au.org.aekos.service.metric.aspect;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import au.org.aekos.controller.ApiV1MaintenanceController;
import au.org.aekos.controller.RootController;
import au.org.aekos.controller.SignupController;
import au.org.aekos.service.metric.MetricsQueueItem;
import au.org.aekos.service.metric.MetricsQueueWorker;
import au.org.aekos.service.metric.aspect.ApiMetricsAspectTest2.ApiMetricsAspectTest2Context;
import au.org.aekos.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.service.retrieval.RetrievalService;
import au.org.aekos.service.search.SearchService;

/**
 * Testing the pointcuts and that the correct stats are recorded for thrown exceptions.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ApiMetricsAspectTest2Context.class)
@WebAppConfiguration
@ActiveProfiles({"test", "force-only-ApiMetricsAspectTest2"})
public class ApiMetricsAspectTest2 {

	private static final String TEXT_CSV_MIME = "text/csv";
	
	@Autowired
	public TestFriendlyCounterService counterService;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(this.wac).build();
	}
	
	@After
	public void after() {
		counterService.counts.clear();
	}
	
	/**
	 * Does the @AfterThrowing for the getTraitVocab.json call work?
	 */
	@Test
	public void testGetTraitVocabJson01() throws Exception {
		try {
			mockMvc.perform(get("/v1/getTraitVocab.json"))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_GET_TRAIT_VOCAB_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the getEnvironmentalVariableVocab.json call work?
	 */
	@Test
	public void testGetEnvironmentalVariableVocabJson01() throws Exception {
		try {
			mockMvc.perform(get("/v1/getEnvironmentalVariableVocab.json"))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_GET_ENVIRONMENTAL_VARIABLE_VOCAB_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the speciesAutocomplete.json call work?
	 */
	@Test
	public void testSpeciesAutocompleteJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesAutocomplete.json");
		uriBuilder.addParameter("q", "t");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_AUTOCOMPLETE_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the getTraitsBySpecies.json call work?
	 */
	@Test
	public void testGetTraitsBySpeciesJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getTraitsBySpecies.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_GET_TRAITS_BY_SPECIES_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the getSpeciesByTrait.json call work?
	 */
	@Test
	public void testGetSpeciesByTraitJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getSpeciesByTrait.json");
		uriBuilder.addParameter("traitName", "averageHeight");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_GET_SPECIES_BY_TRAIT_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the getEnvironmentBySpecies.json call work?
	 */
	@Test
	public void testGetEnvironmentBySpeciesJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getEnvironmentBySpecies.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_GET_ENVIRONMENT_BY_SPECIES_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the speciesSummary.json call work?
	 */
	@Test
	public void testSpeciesSummaryJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesSummary.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_SUMMARY_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the allSpeciesData.json call work?
	 */
	@Test
	public void testAllSpeciesDataJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData.json");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ALL_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the allSpeciesData.csv call work?
	 */
	@Test
	public void testAllSpeciesDataCsv01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData.csv");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ALL_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the allSpeciesData with Accept=json call work?
	 */
	@Test
	public void testAllSpeciesData01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", MediaType.APPLICATION_JSON_VALUE))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ALL_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the allSpeciesData with Accept=csv call work?
	 */
	@Test
	public void testAllSpeciesData02() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", TEXT_CSV_MIME))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ALL_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the speciesData.json call work?
	 */
	@Test
	public void testSpeciesDataJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesData.json");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the speciesData.csv call work?
	 */
	@Test
	public void testSpeciesDataCsv01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesData.csv");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the speciesData with Accept=json call work?
	 */
	@Test
	public void testSpeciesData01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", MediaType.APPLICATION_JSON_VALUE))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the speciesData with Accept=csv call work?
	 */
	@Test
	public void testSpeciesData02() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", TEXT_CSV_MIME))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_SPECIES_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the environmentData.json call work?
	 */
	@Test
	public void testEnvironmentDataJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/environmentData.json");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("envVarName", "var1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ENVIRONMENT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the environmentData.csv call work?
	 */
	@Test
	public void testEnvironmentDataCsv01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/environmentData.csv");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("envVarName", "var1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ENVIRONMENT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the environmentData with Accept=json call work?
	 */
	@Test
	public void testEnvironmentData01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/environmentData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("envVarName", "var1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", MediaType.APPLICATION_JSON_VALUE))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ENVIRONMENT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the environmentData with Accept=csv call work?
	 */
	@Test
	public void testEnvironmentData02() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/environmentData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("envVarName", "var1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", TEXT_CSV_MIME))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_ENVIRONMENT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the traitData.json call work?
	 */
	@Test
	public void testTraitDataJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/traitData.json");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("traitName", "trait1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_TRAIT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	/**
	 * Does the @AfterThrowing for the traitData.csv call work?
	 */
	@Test
	public void testTraitDataCsv01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/traitData.csv");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("traitName", "trait1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build()))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_TRAIT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the traitData with Accept=json call work?
	 */
	@Test
	public void testTraitData01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/traitData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("traitName", "trait1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", MediaType.APPLICATION_JSON_VALUE))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_TRAIT_DATA_DOT_JSON_ERRORS_COUNTER.getFullName()), is(1));
	}

	/**
	 * Does the @AfterThrowing for the traitData with Accept=csv call work?
	 */
	@Test
	public void testTraitData02() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/traitData");
		uriBuilder.addParameter("speciesName", "species1");
		uriBuilder.addParameter("traitName", "trait1");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		try {
			mockMvc.perform(get(uriBuilder.build())
					.header("Accept", TEXT_CSV_MIME))
			    .andExpect(status().isOk());
		} catch (NestedServletException e) { /* swallow so we can check the pointcut worked */ }
		assertThat(counterService.counts.get(ApiMetricsAspect.V1_TRAIT_DATA_DOT_CSV_ERRORS_COUNTER.getFullName()), is(1));
	}
	
	@Configuration
	@ComponentScan(
		basePackages={"au.org.aekos.service.metric", "au.org.aekos.controller"},
		excludeFilters={
			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=MetricsQueueWorker.class),
			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=RootController.class),
			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=SignupController.class),
			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1MaintenanceController.class)
		})
	@EnableAspectJAutoProxy(proxyTargetClass=true)
	@Profile("force-only-ApiMetricsAspectTest2")
	static class ApiMetricsAspectTest2Context {
		
		@Bean
		public Dataset metricsDS() {
			return DatasetFactory.create();
		}
		
		@Bean
		public Model metricsModel(Dataset metricsDS) {
			return metricsDS.getDefaultModel();
		}
		
		@Bean
		public BlockingQueue<MetricsQueueItem> metricsInnerQueue() {
			return new LinkedBlockingDeque<>(10);
		}
		
		@Bean
		public SearchService searchService() throws IOException {
			SearchService result = mock(SearchService.class);
			when(result.speciesAutocomplete(any(), anyInt())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getTraitVocabData()).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getEnvironmentalVariableVocabData()).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getTraitBySpecies(any(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getSpeciesByTrait(any(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getEnvironmentBySpecies(any(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
			return result;
		}
		
		@Bean
		public RetrievalService retrievalService() throws AekosApiRetrievalException {
			RetrievalService result = mock(RetrievalService.class);
			addAllSpeciesStubs(result);
			addSpeciesStubs(result);
			addEnvStubs(result);
			addTraitStubs(result);
			return result;
		}
		
		private void addAllSpeciesStubs(RetrievalService result) throws AekosApiRetrievalException {
			when(result.getAllSpeciesDataJson(anyInt(), anyInt())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getAllSpeciesDataCsv(anyInt(), anyInt(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
		}

		private void addSpeciesStubs(RetrievalService result) throws AekosApiRetrievalException {
			when(result.getSpeciesDataJson(any(), anyInt(), anyInt())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getSpeciesDataCsv(any(), anyInt(), anyInt(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
		}
		
		private void addEnvStubs(RetrievalService result) throws AekosApiRetrievalException {
			when(result.getEnvironmentalDataJson(any(), any(), anyInt(), anyInt())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getEnvironmentalDataCsv(any(), any(), anyInt(), anyInt(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
		}
		
		private void addTraitStubs(RetrievalService result) throws AekosApiRetrievalException {
			when(result.getTraitDataJson(any(), any(), anyInt(), anyInt())).thenThrow(new TriggerAfterThrowingPointcutException());
			when(result.getTraitDataCsv(any(), any(), anyInt(), anyInt(), any())).thenThrow(new TriggerAfterThrowingPointcutException());
		}
		
		@Bean
		public CounterService counterService() {
			return new TestFriendlyCounterService();
		}
	}
	
	static class TestFriendlyCounterService implements CounterService {
		private final Map<String, Integer> counts = new HashMap<>();
		
		@Override
		public void reset(String metricName) { }
		
		@Override
		public void increment(String metricName) {
			Integer curr = counts.get(metricName);
			if (curr == null) {
				curr = 0;
			}
			curr++;
			counts.put(metricName, curr);
		}
		
		@Override
		public void decrement(String metricName) { }
	}
	
	static class TriggerAfterThrowingPointcutException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
