package au.org.aekos.service.search.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.service.search.index.TermIndexManager;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/genIndexContext-test.xml")
public class GeneratedDataLoadIntegrationTest {
	
	@Autowired
	LoaderClient loaderClient;
	
	@Autowired
	TermIndexManager indexManager;
	
	int min = 97;
	int max = 122;
	
	List<String> traits = Arrays.asList("weather","provenance","terantula","bingo","friendly","majestic");
	
	@Test
	public void loadVolumeTest() throws IOException{
		loaderClient.beginLoad();
		
		for(int x = min; x <= max; x++){
			String speciesName = (char) x + "species"; 
			loaderClient.addSpeciesTraitTermsToIndex(speciesName , traits);
			for(int y = min; y <= max; y++){
				loaderClient.addSpeciesTraitTermsToIndex(speciesName + (char) y, traits);
			}
		}
		loaderClient.endLoad();
	}
}
