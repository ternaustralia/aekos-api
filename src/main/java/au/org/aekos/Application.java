package au.org.aekos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

@SpringBootApplication
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
}
