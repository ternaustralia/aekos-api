package au.org.aekos.service.search.index;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.SpeciesName;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class SpeciesLookupIndexServiceTest {

	@Autowired
	SpeciesLookupIndexServiceImpl speciesIndexService;
	
	@Test
	public void testSomeSearches() throws IOException{
		List<SpeciesName> nameList = speciesIndexService.performSearch("aba", 100, false);
		Assert.assertNotNull(nameList);
		for(SpeciesName nm : nameList){
			System.out.println(nm.getName());
		}
		
	}
	
	
}
