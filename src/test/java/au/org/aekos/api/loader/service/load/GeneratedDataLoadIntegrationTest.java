package au.org.aekos.api.loader.service.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.api.loader.service.load.LoaderClient;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/api/loader/genIndexContext-test.xml")
public class GeneratedDataLoadIntegrationTest {
	
	@Autowired
	private LoaderClient loaderClient;
	
	private final int min = 97;
	private final int max = 122;
	
	private final List<String> traits = Arrays.asList("weather","provenance","terantula","bingo","friendly","majestic");
	
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
