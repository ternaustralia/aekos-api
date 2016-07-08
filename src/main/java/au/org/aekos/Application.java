package au.org.aekos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
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

import au.org.aekos.service.auth.AuthModelFactory;
import au.org.aekos.util.ModelLoader;

@SpringBootApplication
@PropertySource("classpath:/au/org/aekos/aekos-api.properties")
@PropertySource(value="file://${user.home}/aekos-api.properties", ignoreResourceNotFound=true)
public class Application extends SpringBootServletInitializer {

	@Autowired
	private Environment environment;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

    public static void main(String[] args) {
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
    public Model dataModel(ModelLoader loader) {
    	return loader.loadModel();
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

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
    
    @Bean
    public Model metricsModel() {
    	return ModelFactory.createDefaultModel();
    }
    
    @Bean
    public Model authModel(AuthModelFactory factory) {
    	return factory.getInstance();
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
