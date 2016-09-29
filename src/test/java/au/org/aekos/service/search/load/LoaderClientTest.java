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
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class LoaderClientTest {

	@Autowired
	LoaderClient loaderClient;
	
	@Autowired
	TermIndexManager indexManager;
	
	@Test
	public void testLoadSomeDataProcess() throws IOException{
		loaderClient.beginLoad();
		List<String> traits = Arrays.asList("Trait1","Trait2","Trait3","Trait4","Trait5");
		loaderClient.addSpeciesTraitTermsToIndex("mySpecies", traits);
		loaderClient.endLoad();
	}
}
