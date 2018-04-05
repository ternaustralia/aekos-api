package au.org.aekos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import au.org.aekos.service.metric.MetricsQueueItem;
import au.org.aekos.util.AuthAekosJenaModelFactory;
import au.org.aekos.util.CoreDataAekosJenaModelFactory;
import au.org.aekos.util.MetricsAekosJenaModelFactory;

@SpringBootApplication
@PropertySource("classpath:/au/org/aekos/aekos-api.properties")
@PropertySource(value="file://${user.home}/aekos-api.properties", ignoreResourceNotFound=true)
public class Application extends SpringBootServletInitializer {

	private static final String DONT_CALL_BECUASE_DATASET_GETS_AUTOCLOSED = "";
	public static final String API_NAMESPACE_V1_0 = "urn:api.aekos.org.au/1.0/";
	public static final String API_DATA_NAMESPACE = "http://www.aekos.org.au/api/1.0#";  // FIXME move to config FIXME could probably name this better
	
	@Autowired
	private Environment environment;
	
	@Value("${aekos-api.owl-file.location}")
	private String owlFileLocation;
	
	@Value("${aekos-api.metrics-queue-capacity}")
	private int metricsQueueCapacity;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

    public static void main(String[] args) {
    	//org.apache.jena.tdb.TDB.getContext().set(org.apache.jena.query.ARQ.symLogExec,true); // Uncomment to enable TDB/ARQ debugging output
        SpringApplication.run(Application.class, args);
    }
    
    @ControllerAdvice
    static class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {
        public JsonpAdvice() {
        	// FIXME get MIME type changed to application/javascript when JSONP is used and document (Swagger) if possible
            super("callback");
        }
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/v1/**");
            }
        };
    }
    
    @Bean
    public Dataset coreDS(CoreDataAekosJenaModelFactory loader) {
    	return loader.getDatasetInstance();
    }
    
    @Bean
    public String darwinCoreQueryTemplate() throws IOException {
		return getSparqlQuery("darwin-core.rq");
    }
    
    @Bean
    public String darwinCoreCountQueryTemplate() throws IOException {
		return getSparqlQuery("darwin-core-count.rq");
    }
    
    @Bean
    public String darwinCoreCountAllQueryTemplate() throws IOException {
		return getSparqlQuery("darwin-core-count-all.rq");
    }
    
    @Bean
    public String environmentDataQueryTemplate() throws IOException {
    	return getSparqlQuery("environment-data.rq");
    }
    
    @Bean
    public String environmentDataCountQueryTemplate() throws IOException {
    	return getSparqlQuery("environment-data-count.rq");
    }
    
    @Bean
    public String traitDataCountQueryTemplate() throws IOException {
		return getSparqlQuery("trait-data-count.rq");
    }
    
    @Bean
    public String indexLoaderQuery() throws IOException {
		return getSparqlQuery("index-loader.rq");
    }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
    
    @Bean(destroyMethod=DONT_CALL_BECUASE_DATASET_GETS_AUTOCLOSED)
    public Model metricsModel(MetricsAekosJenaModelFactory factory) {
    	return factory.getInstance();
    }
    
    @Bean
    public Dataset metricsDS(MetricsAekosJenaModelFactory factory) {
    	return factory.getDatasetInstance();
    }
    
    @Bean
    public Model authModel(AuthAekosJenaModelFactory factory) {
    	return factory.getInstance();
    }
    
    @Bean
    public OntModel owlModel() {
    	OntModel result = ModelFactory.createOntologyModel();
    	result.read(getClass().getResourceAsStream(owlFileLocation), null, "TURTLE");
    	return result;
    }
    
    @Bean
    public BlockingQueue<MetricsQueueItem> metricsInnerQueue() {
    	return new LinkedBlockingDeque<>(metricsQueueCapacity);
    }
}
