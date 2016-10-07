package au.org.aekos.service.metric.aspect;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.Resource;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import au.org.aekos.controller.ApiV1EnvVarRetrievalController;
import au.org.aekos.controller.ApiV1MaintenanceController;
import au.org.aekos.controller.ApiV1SpeciesRetrievalController;
import au.org.aekos.controller.ApiV1TraitRetrievalController;
import au.org.aekos.controller.RetrievalResponseHeader;
import au.org.aekos.controller.RootController;
import au.org.aekos.controller.SignupController;
import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataParams;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.service.metric.MetricsQueueItem;
import au.org.aekos.service.metric.MetricsQueueWorker;
import au.org.aekos.service.metric.MetricsStorageService;
import au.org.aekos.service.metric.RequestRecorder.RequestType;
import au.org.aekos.service.retrieval.AekosApiRetrievalException;
import au.org.aekos.service.retrieval.RetrievalService;
import au.org.aekos.service.search.SearchService;

/**
 * Testing the pointcuts and that the correct stats are recorded for successful calls.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class ApiMetricsAspectTest {

	private static final String TEXT_CSV_MIME = "text/csv";
	
	@Resource(name="metricsInnerQueue")
	private BlockingQueue<MetricsQueueItem> metricsInnerQueue;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(this.wac).build();
	}
	
	/**
	 * Does the getTraitVocab.json call work?
	 */
	@Test
	public void testGetTraitVocabJson01() throws Exception {
		mockMvc.perform(get("/v1/getTraitVocab.json"))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_TRAIT_VOCAB));
	}
	
	/**
	 * Does the getEnvironmentalVariableVocab.json call work?
	 */
	@Test
	public void testGetEnvironmentalVariableVocabJson01() throws Exception {
		mockMvc.perform(get("/v1/getEnvironmentalVariableVocab.json"))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_ENVVAR_VOCAB));
	}
	
	/**
	 * Does the speciesAutocomplete.json call work?
	 */
	@Test
	public void testSpeciesAutocompleteJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesAutocomplete.json");
		uriBuilder.addParameter("q", "t");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequestAutocomplete(any(), eq(RequestType.V1_SPECIES_AUTOCOMPLETE), eq("t"));
	}
	
	/**
	 * Does the getTraitsBySpecies.json call work?
	 */
	@Test
	public void testGetTraitsBySpeciesJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getTraitsBySpecies.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequestWithSpecies(any(), eq(RequestType.V1_TRAIT_BY_SPECIES), eq(new String[] {"Tachyglossus+aculeatus"}), eq(1), eq(15));
	}
	
	/**
	 * Does the getSpeciesByTrait.json call work?
	 */
	@Test
	public void testGetSpeciesByTraitJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getSpeciesByTrait.json");
		uriBuilder.addParameter("traitName", "averageHeight");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequestWithTraitsOrEnvVars(any(), eq(RequestType.V1_SPECIES_BY_TRAIT), eq(new String[] {"averageHeight"}), eq(1), eq(15));
	}
	
	/**
	 * Does the getEnvironmentBySpecies.json call work?
	 */
	@Test
	public void testGetEnvironmentBySpeciesJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/getEnvironmentBySpecies.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		uriBuilder.addParameter("pageNum", "1");
		uriBuilder.addParameter("pageSize", "15");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequestWithSpecies(any(), eq(RequestType.V1_ENVIRONMENT_BY_SPECIES), eq(new String[] {"Tachyglossus+aculeatus"}), eq(1), eq(15));
	}
	
	/**
	 * Does the speciesSummary.json call work?
	 */
	@Test
	public void testGetSpeciesSummaryJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/speciesSummary.json");
		uriBuilder.addParameter("speciesName", "Tachyglossus aculeatus");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_SPECIES_SUMMARY), eq(new String[] {"Tachyglossus+aculeatus"}));
	}
	
	/**
	 * Does the allSpeciesData.json call work?
	 */
	@Test
	public void testAllSpeciesDataJson01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData.json");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_ALL_SPECIES_DATA_JSON), eq(new String[] {}), eq(new String[] {}), eq(0), eq(42));
	}
	
	/**
	 * Does the allSpeciesData.csv call work?
	 */
	@Test
	public void testAllSpeciesDataCsv01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData.csv");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		mockMvc.perform(get(uriBuilder.build()))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_ALL_SPECIES_DATA_CSV), eq(new String[] {}), eq(new String[] {}), eq(0), eq(42));
	}
	
	/**
	 * Does the allSpeciesData with Accept=json call work?
	 */
	@Test
	public void testAllSpeciesData01() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		mockMvc.perform(get(uriBuilder.build())
				.header("Accept", MediaType.APPLICATION_JSON_VALUE))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_ALL_SPECIES_DATA_JSON), eq(new String[] {}), eq(new String[] {}), eq(0), eq(42));
	}
	
	/**
	 * Does the allSpeciesData with Accept=csv call work?
	 */
	@Test
	public void testAllSpeciesData02() throws Exception {
		URIBuilder uriBuilder = new URIBuilder("/v1/allSpeciesData");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("rows", "42");
		mockMvc.perform(get(uriBuilder.build())
				.header("Accept", TEXT_CSV_MIME))
	        .andExpect(status().isOk());
		assertThat(metricsInnerQueue.size(), is(1));
		MetricsStorageService metricsStore = mock(MetricsStorageService.class);
		metricsInnerQueue.remove().doPersist(metricsStore);
		verify(metricsStore).recordRequest(any(), eq(RequestType.V1_ALL_SPECIES_DATA_CSV), eq(new String[] {}), eq(new String[] {}), eq(0), eq(42));
	}
	
	@Configuration
	@ComponentScan(
			basePackages={"au.org.aekos.service.metric", "au.org.aekos.controller"},
			excludeFilters={
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=MetricsQueueWorker.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=RootController.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=SignupController.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1EnvVarRetrievalController.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1MaintenanceController.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1SpeciesRetrievalController.class),
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1TraitRetrievalController.class)
			})
	@EnableAspectJAutoProxy(proxyTargetClass=true)
	static class TestContext {
		
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
		public SearchService searchService() {
			return mock(SearchService.class);
		}
		
		@Bean
		public RetrievalService retrievalService() throws AekosApiRetrievalException {
			RetrievalService result = mock(RetrievalService.class);
			AbstractParams params = new SpeciesDataParams(0, 20, Collections.emptyList());
			SpeciesDataResponse allSpeciesResponse = new SpeciesDataResponse(new ResponseHeader(123, 1, 7, 42, params), Collections.emptyList());
			when(result.getAllSpeciesDataJson(anyInt(), anyInt())).thenReturn(allSpeciesResponse);
			when(result.getAllSpeciesDataCsv(anyInt(), anyInt(), any())).thenReturn(RetrievalResponseHeader.newInstance(allSpeciesResponse));
			return result;
		}
		
		@Bean
		public CounterService counterService() {
			return new CounterService() {
				@Override
				public void reset(String metricName) { }
				
				@Override
				public void increment(String metricName) { }
				
				@Override
				public void decrement(String metricName) { }
			};
		}
	}
}

