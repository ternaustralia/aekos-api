package au.org.aekos.service.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.TraitVocabEntry;
import au.org.aekos.service.search.load.LoaderClient;
import org.junit.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class LuceneSearchServiceTest {
	@Autowired
	LoaderClient loader;
	@Autowired
	SearchService searchService;
	
   @Test
	public void test() throws IOException{
	   loader.beginLoad();
	   loader.addSpeciesTraitTermToIndex("species", "traitOne");
	   loader.addSpeciesTraitTermToIndex("species", "traitTwo");
	   loader.addSpeciesTraitTermToIndex("species", "traitThree");
	   loader.addSpeciesTraitTermToIndex("speciesxx", "traitOne");
	   loader.addSpeciesTraitTermToIndex("speciesxxx", "traitOne");
	   loader.endLoad();
	   
	   List<TraitVocabEntry> results = searchService.getTraitBySpecies(Arrays.asList("species"));
	   Assert.assertTrue(results != null && results.size() == 3);
	   Assert.assertEquals("traitOne", results.get(0).getCode());
	   Assert.assertEquals("traitThree", results.get(1).getCode());
	   Assert.assertEquals("traitTwo", results.get(2).getCode());
	   
	   List<TraitVocabEntry> results2 = searchService.getTraitBySpecies(Arrays.asList("species","speciesxx","speciesxxx"));
	   System.out.println(results2.size() + "*************************************************");
	   
	   
	   
	   
	}
	
	
}
