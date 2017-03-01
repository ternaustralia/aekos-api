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
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
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
		public SearchService searchService() {
			SearchService result = mock(SearchService.class);
			when(result.getTraitVocabData()).thenReturn(
					Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("averageHeight", "Average Height")));
			return result;
		}
	}
}
