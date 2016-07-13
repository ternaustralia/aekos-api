package au.org.aekos.service.vocab;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages="au.org.aekos.service.vocab")
@PropertySource("classpath:/au/org/aekos/vocabService-test.properties")
public class JenaVocabServiceTest_Application {

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
