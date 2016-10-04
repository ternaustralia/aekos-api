package au.org.aekos.service.retrieval;

import static au.org.aekos.EnvironmentDataRecordMatcher.isEnvRecord;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import au.org.aekos.model.EnvironmentDataResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=JenaRetrievalServiceTestContext.class)
public class JenaRetrievalServiceTestSpring2 {

	@Autowired
	private JenaRetrievalService objectUnderTest;

	/**
	 * Can we correctly query for the environmental data so we only try to gather information for visits
	 * that are actually relevant (basically, is the VALUES clause correct)?
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetEnvironmentalDataJson01() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("Abutilon otocarpum"), Collections.emptyList(), 0, 10);
		assertThat(result.getResponse().size(), is(4));
		assertThat(result.getResponse(), hasItems(
			isEnvRecord("2014-04-29", "aekos.org.au/collection/sydney.edu.au/DERG/Plum%20Pudding"),
			isEnvRecord("2013-04-29", "aekos.org.au/collection/sydney.edu.au/DERG/Tobermorey%20West"),
			isEnvRecord("2013-04-29", "aekos.org.au/collection/sydney.edu.au/DERG/Tobermorey%20East"),
			isEnvRecord("2014-04-29", "aekos.org.au/collection/sydney.edu.au/DERG/Tobermorey%20East")));
	}
}

@Configuration
@ComponentScan(
	basePackages={"au.org.aekos.service.retrieval"},
	excludeFilters={
		@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=au.org.aekos.service.retrieval.JossDataProvisionService.class)
	})
class JenaRetrievalServiceTestContext {
	
	@Bean
    public Dataset coreDS() {
    	Dataset result = DatasetFactory.create();
    	Model m = result.getDefaultModel();
    	m.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/JenaRetrievalServiceTestSpring2-data.ttl"), null, "TURTLE");
		return result;
    }
	
    @Bean public String darwinCoreQueryTemplate() throws IOException { return getSparqlQuery("darwin-core.rq"); }
    @Bean public String darwinCoreCountQueryTemplate() throws IOException { return getSparqlQuery("darwin-core-count.rq"); }
    @Bean public String darwinCoreCountAllQueryTemplate() throws IOException { return getSparqlQuery("darwin-core-count-all.rq"); }
    @Bean public String environmentDataQueryTemplate() throws IOException { return getSparqlQuery("environment-data.rq"); }
    @Bean public String environmentDataCountQueryTemplate() throws IOException { return getSparqlQuery("environment-data-count.rq"); }
    @Bean public String traitDataCountQueryTemplate() throws IOException { return getSparqlQuery("trait-data-count.rq"); }
    @Bean public String indexLoaderQuery() throws IOException { return getSparqlQuery("index-loader.rq"); }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}