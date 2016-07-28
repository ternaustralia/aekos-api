package au.org.aekos.service.search;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.SpeciesSummary;
import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.search.index.TermIndexManager;
import au.org.aekos.service.search.load.LoaderClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class LuceneSearchServiceTestSpring {
	
	@Autowired
	private LoaderClient loader;
	
	@Autowired
	private SearchService objectUnderTest;
	
	@Autowired
	private TermIndexManager indexManager;
	
	@After
	public void cleanTermIndexOutAfterEachTest() throws IOException {
		indexManager.closeTermIndex();
	}
	
	/**
	 * Can we get the expected traits for a species?
	 */
	@Test
	public void testGetTraitBySpecies01() throws IOException{
	   loadMockSpeciesToTraitData();
	   PageRequest pr = new PageRequest(0, 0);
	   List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitBySpecies(Arrays.asList("speciesOne"), pr);
	   assertEquals(3, result.size());
	   assertEquals("traitOne", result.get(0).getCode());
	   assertEquals("traitThree", result.get(1).getCode());
	   assertEquals("traitTwo", result.get(2).getCode());
	}

	/**
	 * Can we get traits for multiple species?
	 */
	@Test
	public void testGetTraitBySpecies02() throws IOException{
	   loadMockSpeciesToTraitData();
	   PageRequest pr = new PageRequest(0, 0);
	   List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitBySpecies(Arrays.asList("speciesOne","speciesTwo","speciesThree"),pr);
	   assertEquals(3, result.size());
	   assertEquals("traitOne", result.get(0).getCode());
	   assertEquals("traitThree", result.get(1).getCode());
	   assertEquals("traitTwo", result.get(2).getCode());
	}
	
	/**
	 * Can we get species using a trait?
	 */
	@Test
	public void testGetSpeciesByTrait01() throws IOException{
	   loadMockSpeciesToTraitData();
	   PageRequest pr = new PageRequest(0, 0);
	   List<SpeciesName> result = objectUnderTest.getSpeciesByTrait(Arrays.asList("traitOne"), pr);
	   assertEquals(3, result.size());
	   assertEquals("speciesOne", result.get(0).getName());
	   assertEquals("speciesThree", result.get(1).getName());
	   assertEquals("speciesTwo", result.get(2).getName());
	}

	/**
	 * Can we get species using multiple traits?
	 */
	@Test
	public void testGetSpeciesByTrait02() throws IOException{
	   loadMockSpeciesToTraitData();
	   PageRequest pr = new PageRequest(0, 0);
	   List<SpeciesName> result = objectUnderTest.getSpeciesByTrait(Arrays.asList("traitOne","traitTwo","traitThree"), pr);
	   assertEquals(3, result.size());
	   assertEquals("speciesOne", result.get(0).getName());
	   assertEquals("speciesThree", result.get(1).getName());
	   assertEquals("speciesTwo", result.get(2).getName());
	}
	
    /**
     * Can we get environmental variables for a species?
     */
    @Test
	public void testGetEnvironmentBySpecies01() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesEnvironmentTermToIndex("speciesOne", "environmentOne");
	   loader.addSpeciesEnvironmentTermToIndex("speciesTwo", "environmentOne");
	   loader.addSpeciesEnvironmentTermToIndex("speciesTwo", "environmentTwo");
	   loader.endLoad();
	   List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getEnvironmentBySpecies(Arrays.asList("speciesOne"), null);
	   assertEquals(1, result.size());
	   assertEquals("environmentOne", result.get(0).getCode());
    }
    
    /**
     * Can we get the trait vocab data when it exists?
     */
    @Test
	public void testGetTraitVocabData01() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesTraitTermToIndex("species1", "trait one");
	   loader.addSpeciesTraitTermToIndex("species1", "trait two");
	   loader.addSpeciesTraitTermToIndex("species2", "trait two");
	   loader.endLoad();
	   List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getTraitVocabData();
	   assertThat(result.size(), is(2));
	   List<String> codes = result.stream().map(s -> s.getCode()).collect(Collectors.toList());
	   assertThat(codes, hasItems("trait one", "trait two"));
    }
    
    /**
     * Can we get the environmental variable vocab data when it exists?
     */
    @Test
	public void testGetEnvironmentalVariableVocabData01() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesEnvironmentTermToIndex("species1", "env one");
	   loader.addSpeciesEnvironmentTermToIndex("species1", "env two");
	   loader.addSpeciesEnvironmentTermToIndex("species2", "env two");
	   loader.endLoad();
	   List<TraitOrEnvironmentalVariableVocabEntry> result = objectUnderTest.getEnvironmentalVariableVocabData();
	   assertThat(result.size(), is(2));
	   List<String> codes = result.stream().map(s -> s.getCode()).collect(Collectors.toList());
	   assertThat(codes, hasItems("env one", "env two"));
    }
    
    /**
	 * Can we find the expected species?
	 */
	@Test
	public void testPerformSearch01() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("abac", 100);
		assertNotNull(result);
		assertThat(result.size(), is(4));
		List<String> names = result.stream().map(s -> s.getScientificName()).collect(Collectors.toList());
		assertThat(names, hasItems("Abacopteris aspera", "Abacopteris presliana", "Abacopteris sp.", "Abacopteris triphylla"));
	}

	/**
	 * Does the first result start with the queried letter when such a result exists?
	 */
	@Test
	public void testPerformSearch02() throws IOException{
		loadTaxaNamesCsv();
		List<SpeciesSummary> result = objectUnderTest.speciesAutocomplete("m", 100);
		assertEquals(100, result.size());
		SpeciesSummary species = result.get(0);
		assertEquals("Mariosousa millefolia", species.getScientificName());
	}
	
	private void loadTaxaNamesCsv() throws IOException {
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("au/org/aekos/test-taxa_names.csv");
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);) {
			String line = null;
			loader.beginLoad();
			while ((line = reader.readLine()) != null) {
				loader.addSpecies(line, 123);
			}
			loader.endLoad();
		}
	}
	
	private void loadMockSpeciesToTraitData() throws IOException {
		loader.beginLoad();
		loader.addSpeciesTraitTermToIndex("speciesOne", "traitOne");
		loader.addSpeciesTraitTermToIndex("speciesOne", "traitTwo");
		loader.addSpeciesTraitTermToIndex("speciesOne", "traitThree");
		loader.addSpeciesTraitTermToIndex("speciesTwo", "traitOne");
		loader.addSpeciesTraitTermToIndex("speciesThree", "traitOne");
		loader.endLoad();
	}
}
