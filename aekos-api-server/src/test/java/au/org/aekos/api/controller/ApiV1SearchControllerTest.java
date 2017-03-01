package au.org.aekos.api.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import au.org.aekos.api.controller.ApiV1SearchControllerTest.ApiV1SearchControllerTestContext;
import au.org.aekos.api.model.SpeciesName;
import au.org.aekos.api.model.SpeciesSummary;
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.api.service.search.PageRequest;
import au.org.aekos.api.service.search.SearchService;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableWebMvc
@WebAppConfiguration
@ContextConfiguration(classes=ApiV1SearchControllerTestContext.class)
@ActiveProfiles({"test", "force-only-ApiV1SearchControllerTest"})
public class ApiV1SearchControllerTest {
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(this.wac).build();
	}
	
	/**
	 * Can we get the trait vocab?
	 */
	@Test
	public void testGetTraitVocab01() throws Exception {
		mockMvc.perform(get("/v1/getTraitVocab.json")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[{\"code\":\"averageHeight\",\"label\":\"Average Height\"}]"));
	}
	
	/**
	 * Can we get the environmental vocab?
	 */
	@Test
	public void testGetEnvironmentalVariableVocab01() throws Exception {
		mockMvc.perform(get("/v1/getEnvironmentalVariableVocab.json")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[{\"code\":\"soilType\",\"label\":\"Soil Type\"}]"));
	}
	
	/**
	 * Can we do a species autocomplete?
	 */
	@Test
	public void testSpeciesAutocomplete01() throws Exception {
		mockMvc.perform(get("/v1/speciesAutocomplete.json")
				.param("q", "aca")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("["
            		+ "{\"id\":\"-520822480\",\"speciesName\":\"Acacia aneura\",\"recordsHeld\":3},"
            		+ "{\"id\":\"1984720018\",\"speciesName\":\"Acacia applanata\",\"recordsHeld\":1}"
            		+ "]"));
	}
	
	/**
	 * Can we get traits using a species name?
	 */
	@Test
	public void testGetTraitsBySpecies01() throws Exception {
		mockMvc.perform(get("/v1/getTraitsBySpecies.json")
				.param("speciesName", "Acacia aneura")
				.param("pageNum", "1")
				.param("pageSize", "10")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[{\"code\":\"averageHeight\",\"label\":\"Average Height\"}]"));
	}
	
	/**
	 * Can we get species using a trait name?
	 */
	@Test
	public void testGetSpeciesByTrait01() throws Exception {
		mockMvc.perform(get("/v1/getSpeciesByTrait.json")
				.param("traitName", "averageHeight")
				.param("pageNum", "1")
				.param("pageSize", "10")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[{\"name\":\"Acacia aneura\",\"id\":\"-520822480\"}]"));
	}
	
	/**
	 * Can we get environmental variables using a species name?
	 */
	@Test
	public void testGetEnvironmentBySpecies01() throws Exception {
		mockMvc.perform(get("/v1/getEnvironmentBySpecies.json")
				.param("speciesName", "Acacia aneura")
				.param("pageNum", "1")
				.param("pageSize", "10")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[{\"code\":\"soilType\",\"label\":\"Soil Type\"}]"));
	}
	
	/**
	 * Can we get a species summary?
	 */
	@Test
	public void testSpeciesSummary01() throws Exception {
		mockMvc.perform(get("/v1/speciesSummary.json")
				.param("speciesName", "Acacia applanata")
    			.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("["
            		+ "{\"id\":\"1984720018\",\"speciesName\":\"Acacia applanata\",\"recordsHeld\":1}"
            		+ "]"));
	}
	
	@TestConfiguration
	@ComponentScan(
			basePackages={"au.org.aekos.api.controller"},
			includeFilters={
					@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1SearchController.class),
			},
			excludeFilters={
					@Filter(type=FilterType.REGEX, pattern="au.org.aekos.api.(?!controller.ApiV1Search).*"),	
			})
	@Profile("force-only-ApiV1SearchControllerTest")
	static class ApiV1SearchControllerTestContext {
		@Bean
		public SearchService searchService() throws Throwable {
			SearchService result = mock(SearchService.class);
			when(result.getTraitVocabData()).thenReturn(
					Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("averageHeight", "Average Height")));
			when(result.getEnvironmentalVariableVocabData()).thenReturn(
					Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("soilType", "Soil Type")));
			when(result.speciesAutocomplete("aca", 50)).thenReturn(
					Arrays.asList(new SpeciesSummary("Acacia aneura", 3), new SpeciesSummary("Acacia applanata", 1)));
			when(result.getTraitBySpecies(Arrays.asList("Acacia aneura"), new PageRequest(1, 10))).thenReturn(
					Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("averageHeight", "Average Height")));
			when(result.getSpeciesByTrait(Arrays.asList("averageHeight"), new PageRequest(1, 10))).thenReturn(
					Arrays.asList(new SpeciesName("Acacia aneura")));
			when(result.getEnvironmentBySpecies(Arrays.asList("Acacia aneura"), new PageRequest(1, 10))).thenReturn(
					Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("soilType", "Soil Type")));
			when(result.speciesAutocomplete("Acacia applanata", 1)).thenReturn(
					Arrays.asList(new SpeciesSummary("Acacia applanata", 1)));
			return result;
		}
	}
}
