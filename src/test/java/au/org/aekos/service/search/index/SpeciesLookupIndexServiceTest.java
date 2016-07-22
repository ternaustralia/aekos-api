package au.org.aekos.service.search.index;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
	private SpeciesLookupIndexServiceImpl objectUnderTest;
	
	/**
	 * Can we find the expected species?
	 */
	@Test
	public void testPerformSearch01() throws IOException{
		List<SpeciesName> result = objectUnderTest.performSearch("abac", 100, false);
		Assert.assertNotNull(result);
		assertThat(result.size(), is(4));
		List<String> names = result.stream().map(s -> s.getName()).collect(Collectors.toList());
		assertThat(names, hasItems("Abacopteris aspera", "Abacopteris presliana", "Abacopteris sp.", "Abacopteris triphylla"));
	    
		result = objectUnderTest.performSearch("m", 100, false);
		Assert.assertTrue(result.size() > 0);
		SpeciesName species = result.get(0);
		Assert.assertEquals("m", species.getName().substring(0,1).toLowerCase() );
		
	
	
	}
}
