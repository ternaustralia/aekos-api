package au.org.aekos.service.search.index;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/indexContext-test.xml")
public class LuceneIndexBuilderServiceTest {
	
	@Autowired
	LuceneIndexBuilderServiceImpl indexBuilder;
	
	@Autowired
	SpeciesLookupIndexServiceImpl speciesIndexService;
	
	@Test
	public void testIndexPathFunctions(){
		Assert.assertNotNull(indexBuilder);
		System.out.println(indexBuilder.getIndexPath());
		Assert.assertTrue(indexBuilder.ensureIndexPathExists());
	}
	
	@Test //Having issues 
	public void readSpeciesCsvFromClasspath() throws URISyntaxException, IOException{
		String speciesResourcePath = indexBuilder.getSpeciesResourcePath();
		Assert.assertNotNull(speciesResourcePath);
		System.out.println(speciesResourcePath);
		Path path =  Paths.get(ClassLoader.getSystemResource(speciesResourcePath).toURI());
		Assert.assertNotNull(path);
		BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
		String line = null;  
		while ((line = reader.readLine()) != null){
		}
		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(path)) {
			stream.forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAutobuildOfSpeciesIndex(){
		RAMDirectory rd = speciesIndexService.getSpeciesIndex();
		Assert.assertNotNull(rd);
		
	}
	
	
	
	
}
