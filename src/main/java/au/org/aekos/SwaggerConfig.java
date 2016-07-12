package au.org.aekos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.collect.Lists.*;

@Configuration
@EnableSwagger2
public class SwaggerConfig {   
	
	@Value("${aekos-api.version}")
	private String apiVersion;
	
	@Bean
	public Docket api() { 
		return new Docket(DocumentationType.SWAGGER_2)  
				.apiInfo(apiInfo())
				.select()                                  
				.apis(RequestHandlerSelectors.any())              
				.paths(PathSelectors.ant("/v1/*"))                          
				.build()
				.securitySchemes(newArrayList(apiKey()))
		        .securityContexts(newArrayList(securityContext()));
	}

	private ApiKey apiKey() {
		return new ApiKey("aekos_key", "api_key", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex("/v1.*"))
				.build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope
		= new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return newArrayList(
				new SecurityReference("aekos_key", authorizationScopes));
	}

	@Bean
	SecurityConfiguration security() {
		return new SecurityConfiguration(
				"test-app-client-id",
				"test-app-client-secret",
				"test-app-realm",
				"test-app",
				"apiKey",
				ApiKeyVehicle.HEADER, 
				"api_key", 
				"," /*scope separator*/);
	}
	
	private ApiInfo apiInfo() {
		ApiInfo apiInfo = new ApiInfo(
				"ÆKOS REST API",
				"The ÆKOS API is used for M2M REST access to ÆKOS ecological data.",
				apiVersion, 
				"TODO - ÆKOS API TOS",
				new Contact("TERN Ecoinformatics", "http://www.aekos.org.au", "api@aekos.org.au"),
				"TODO - License of API",
				"http://api.aekos.org.au");
		return apiInfo;
	}
}


