package au.org.aekos.service.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.SpeciesName;
import au.org.aekos.model.TraitOrEnvironmentalVariableVocabEntry;
import au.org.aekos.service.search.index.TermIndexManager;
import au.org.aekos.service.search.load.LoaderClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class LuceneSearchServiceTest {
	@Autowired
	LoaderClient loader;
	
	@Autowired
	SearchService searchService;
	
	@Autowired
	TermIndexManager indexManager;
	
   @Test
	public void testTraitSpeciesTermsSearches() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesTraitTermToIndex("species", "traitOne");
	   loader.addSpeciesTraitTermToIndex("species", "traitTwo");
	   loader.addSpeciesTraitTermToIndex("species", "traitThree");
	   loader.addSpeciesTraitTermToIndex("speciesxx", "traitOne");
	   loader.addSpeciesTraitTermToIndex("speciesxxx", "traitOne");
	   loader.endLoad();
	   
	   PageRequest pr = new PageRequest(0, 0);
	   
	   List<TraitOrEnvironmentalVariableVocabEntry> results = searchService.getTraitBySpecies(Arrays.asList("species"), pr);
	   Assert.assertTrue(results != null && results.size() == 3);
	   Assert.assertEquals("traitOne", results.get(0).getCode());
	   Assert.assertEquals("traitThree", results.get(1).getCode());
	   Assert.assertEquals("traitTwo", results.get(2).getCode());
	   
	   List<TraitOrEnvironmentalVariableVocabEntry> results2 = searchService.getTraitBySpecies(Arrays.asList("species","speciesxx","speciesxxx"),pr);
	   System.out.println(results2.size() + "*************************************************");
	   
	   List<SpeciesName> speciesList = searchService.getSpeciesByTrait(Arrays.asList("traitOne"),pr);
	   Assert.assertEquals(3, speciesList.size());
	   
	   speciesList = searchService.getSpeciesByTrait(Arrays.asList("traitOne","traitTwo","traitThree"),pr);
	   Assert.assertEquals(3, speciesList.size());
	   
	   indexManager.closeTermIndex();
	}
   
    @Test
	public void testSpeciesEnvironmentTermsSearches() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesEnvironmentTermToIndex("species", "environmentOne");
	   loader.addSpeciesEnvironmentTermToIndex("species", "environmentOne");
	   loader.addSpeciesEnvironmentTermToIndex("species", "environmentTwo");
	   loader.endLoad();
	   
	   List<TraitOrEnvironmentalVariableVocabEntry> environmentList = searchService.getEnvironmentBySpecies(Arrays.asList("species"), null);
	   Assert.assertEquals(2, environmentList.size());
	   indexManager.closeTermIndex();
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
	   List<TraitOrEnvironmentalVariableVocabEntry> result = searchService.getTraitVocabData();
	   assertEquals(2, result.size());
	   indexManager.closeTermIndex();
    }
}
