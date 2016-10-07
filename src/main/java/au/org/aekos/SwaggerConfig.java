package au.org.aekos;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

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

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Value("${aekos-api.version}")
	private String apiVersion;
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.ignoredParameterTypes(Writer.class)
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
				getDescription(),
				apiVersion,
				"http://www.ecoinformatics.org.au/licensing_and_attributions",
				new Contact("TERN Ecoinformatics", "http://www.ecoinformatics.org.au", "api@aekos.org.au"),
				"Licensing and attributions",
				"http://www.ecoinformatics.org.au/licensing_and_attributions");
		return apiInfo;
	}

	private String getDescription() {
		try {
			InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("reference/api-description.md");
			OutputStream out = new ByteArrayOutputStream();
			StreamUtils.copy(sparqlIS, out);
			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load the API description", e);
		}
	}
}
