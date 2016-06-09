package au.org.aekos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import au.org.aekos.util.ModelLoader;

@SpringBootApplication
@ImportResource("application-context.xml")
@PropertySource("classpath:/au/org/aekos/aekos-api.properties")
@PropertySource(value="file://${user.home}/aekos-api.properties", ignoreResourceNotFound=true)
public class Application extends SpringBootServletInitializer {

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
    public Model model(ModelLoader loader) {
    	return loader.loadModel();
    }
    
    @Bean
    public String darwinCoreQueryTemplate() throws IOException {
    	InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/sparql/darwin-core.rq");
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
    }
}
