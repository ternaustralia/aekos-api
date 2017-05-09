package au.org.aekos.api.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:/au/org/aekos/api/producer/aekos-api-producer.properties")
@PropertySource(value="file://${user.home}/aekos-api.properties", ignoreResourceNotFound=true)
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
