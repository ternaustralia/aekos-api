package au.org.aekos.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import au.org.aekos.api.Application;
import au.org.aekos.api.controller.ApiV1SearchControllerTest.ApiV1SearchControllerTestContext;
import au.org.aekos.api.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.api.service.search.LuceneSearchService;

@RunWith(SpringRunner.class)
@OverrideAutoConfiguration(enabled=true)
@WebMvcTest(
		excludeAutoConfiguration={
				ApiV1SearchControllerTestContext.class,
				Application.class}
//		controllers=ApiV1SearchController.class,
//		includeFilters={
//			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiV1SearchController.class),
//		},
//		excludeFilters={
//			@Filter(type=FilterType.REGEX, pattern="au.org.aekos.api.controller.(?!ApiV1Search).*"),
//			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=ApiMetricsAspect.class),
//			@Filter(type=FilterType.ANNOTATION, classes=Configuration.class),
//			@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=MetricsAekosJenaModelFactory.class),
//		}
		)
// This test passes but the framework is super painful to work with. How do you stop it from including the whole universe when it starts!!!
// e.g: the TDB instances all start up. Where is it pulling that config from?
public class ApiV1SearchControllerTestAwful {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LuceneSearchService searchService;

    @Test
    public void testExample() throws Exception {
        given(this.searchService.getTraitVocabData())
                .willReturn(Arrays.asList(new TraitOrEnvironmentalVariableVocabEntry("trait1", "Trait One")));
        this.mvc.perform(get("/v1/getTraitVocab.json")
        			.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"code\":\"trait1\",\"label\":\"Trait One\"}]"));
    }

}