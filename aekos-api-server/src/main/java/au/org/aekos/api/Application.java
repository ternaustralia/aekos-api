package au.org.aekos.api;

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
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import au.org.aekos.api.loader.service.load.LuceneIndexingService;
import au.org.aekos.api.loader.service.load.LuceneLoaderClient;
import au.org.aekos.api.loader.util.AuthAekosJenaModelFactory;
import au.org.aekos.api.loader.util.CoreDataAekosJenaModelFactory;
import au.org.aekos.api.loader.util.MetricsAekosJenaModelFactory;
import au.org.aekos.api.service.metric.MetricsQueueItem;

@SpringBootApplication
@ComponentScan(excludeFilters={
	@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=LuceneLoaderClient.class),
	@Filter(type=FilterType.ASSIGNABLE_TYPE, classes=LuceneIndexingService.class)})
@PropertySource("classpath:/au/org/aekos/api/aekos-api.properties")
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
                registry.addMapping(Constants.V1_0 + "/**");
                registry.addMapping(Constants.V1_1 + "/**");
            }
        };
    }
    
    // These two factory beans should live in this project but doing so causes "AbstractMethodError: null"
    // so the workaround is to move them to aekos-api-loader, remove @Service (otherwise the loader finds them) and do this...
    @Bean public MetricsAekosJenaModelFactory metricsAekosJenaModelFactory() { return new MetricsAekosJenaModelFactory(); }
    @Bean public AuthAekosJenaModelFactory authAekosJenaModelFactory() { return new AuthAekosJenaModelFactory(); }
    
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
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/sparql/" + fileName);
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
    
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};
		tomcat.addAdditionalTomcatConnectors(createHttpConnector());
		return tomcat;
	}
	
	private Connector createHttpConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setSecure(false);
		Integer serverPort = environment.getProperty("server.port", Integer.class, 8443);
		Integer serverHttpPort = environment.getProperty("server.http.port", Integer.class, 8080);
		connector.setPort(serverHttpPort);
		connector.setRedirectPort(serverPort);
		return connector;
	}
}
