package au.org.aekos.api.service.search;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.api.loader.service.index.RAMDirectoryTermIndexManager;
import au.org.aekos.api.loader.service.index.TermIndexManager;
import au.org.aekos.api.loader.service.load.LoaderClient;
import au.org.aekos.api.loader.service.load.SpeciesLoaderRecord;
import au.org.aekos.api.model.SpeciesSummary;
import au.org.aekos.api.service.vocab.VocabService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=LuceneSearchServiceTestSpringContext.class)
public class LuceneSearchServiceTestSpring {
	
	@Autowired
	private LoaderClient loader;
	
	@Autowired
	private LuceneSearchService objectUnderTest;
	
	@Autowired
	private TermIndexManager indexManager;
	
	@After
	public void cleanTermIndexOutAfterEachTest() throws IOException {
		indexManager.closeTermIndex();
	}
	
	/**
	 * Can we find the expected species?
	 */
	@Test
	public void testSpeciesAutocomplete01() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("abac", 100);
		assertNotNull(result);
		assertThat(result.size(), is(4));
		List<String> names = result.stream().map(s -> s.getSpeciesName() + "|" + s.getRecordsHeld()).collect(Collectors.toList());
		assertThat(names, hasItems("Abacopteris aspera|1", "Abacopteris presliana|1", "Abacopteris sp.|1", "Abacopteris triphylla|1"));
	}

	/**
	 * Does the first result start with the queried letter when such a result exists?
	 */
	@Test
	public void testSpeciesAutocomplete02() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("m", 100);
		assertEquals(100, result.size());
		SpeciesSummary species = result.get(0);
		assertEquals("Mariosousa millefolia", species.getSpeciesName());
	}
	
	/**
	 * Can we get the facet count for a species when we trigger the 'sub-search'?
	 */
	@Test
	public void testSpeciesAutocomplete03() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("pres", 100);
		assertNotNull(result);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).getSpeciesName(), is("Abacopteris presliana"));
		assertThat(result.get(0).getRecordsHeld(), is(1));
	}
	
	/**
	 * Can we get the facet count for a species when we trigger the 'wildcard-search'?
	 */
	@Test
	public void testSpeciesAutocomplete04() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("reslian", 100);
		assertNotNull(result);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).getSpeciesName(), is("Abacopteris presliana"));
		assertThat(result.get(0).getRecordsHeld(), is(1));
	}
	
	private void loadTaxaNamesCsv() throws IOException {
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("au/org/aekos/api/test-taxa_names.csv");
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);) {
			String line = null;
			loader.beginLoad();
			while ((line = reader.readLine()) != null) {
				SpeciesLoaderRecord minimalRecord = new SpeciesLoaderRecord(line, Collections.emptySet(), "", "", "", "");
				loader.addSpeciesRecord(minimalRecord);
			}
			loader.endLoad();
		}
	}
}

@Configuration
@ComponentScan(
	basePackages={
		"au.org.aekos.api.service.search",
		"au.org.aekos.api.loader.service.index",
		"au.org.aekos.api.loader.service.load"})
class LuceneSearchServiceTestSpringContext {
	
	@Bean
	public TermIndexManager /*override the bean from the component scan*/FSDirectoryTermIndexManager() {
		return new RAMDirectoryTermIndexManager();
	}
	
	@Bean
	public VocabService vocabService() {
		return new StubVocabService();
	}
	
	@Bean
	public Dataset coreDS() {
		return DatasetFactory.create();
	}
	
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
	    PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
	    Properties properties = new Properties();
	    properties.setProperty("lucene.index.writer.commitLimit", "1000");
	    properties.setProperty("lucene.page.defaultResutsPerPage", "200");
		result.setProperties(properties);
		return result;
	}
	
	@Bean public String darwinCoreAndTraitsQuery() { return ""; }
	@Bean public String citationDetailsQuery() { return ""; }
	@Bean public String environmentalVariablesQuery() { return ""; }
}
