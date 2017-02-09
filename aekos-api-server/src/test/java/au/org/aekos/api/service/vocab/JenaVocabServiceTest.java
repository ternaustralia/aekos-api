package au.org.aekos.api.service.vocab;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.api.service.vocab.JenaVocabService;
import au.org.aekos.api.service.vocab.JenaVocabServiceTest.JenaVocabServiceTest_Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=JenaVocabServiceTest_Application.class)
public class JenaVocabServiceTest {

	@Autowired
	private JenaVocabService objectUnderTest;

	/**
	 * Can we get the label when it's present in the OWL file?
	 */
	@Test
	public void testGetLabelForPropertyCode01() {
		String result = objectUnderTest.getLabelForPropertyCode("averageHeight");
		assertThat(result, is("Average Height"));
	}
	
	/**
	 * Is null returned when we ask for a datatype property that isn't in the file?
	 */
	@Test
	public void testGetLabelForPropertyCode02() {
		String result = objectUnderTest.getLabelForPropertyCode("notInOwlFile");
		assertNull("Should be null because it's not in the OWL file", result);
	}
	
	/**
	 * Is null returned when we ask for a datatype property that is in the file but doesn't have a label defined?
	 */
	@Test
	public void testGetLabelForPropertyCode03() {
		String result = objectUnderTest.getLabelForPropertyCode("notInOwlFile");
		assertNull("Should be null because a label isn't defined", result);
	}
	
	@Configuration
	@ComponentScan(basePackages="au.org.aekos.api.service.vocab")
	@PropertySource("classpath:/au/org/aekos/api/vocabService-test.properties")
	public static class JenaVocabServiceTest_Application {

		@Value("${aekos-api.owl-file.location}")
		private String owlFileLocation;
		
	    @Bean
	    public OntModel owlModel() {
	    	OntModel result = ModelFactory.createOntologyModel();
	    	result.read(getClass().getResourceAsStream(owlFileLocation), null, "TURTLE");
	    	return result;
	    }
	    
	    /**
	     * Property placeholder configurer needed to process @Value annotations
	     */
	     @Bean
	     public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
	        return new PropertySourcesPlaceholderConfigurer();
	     }
	}
}
