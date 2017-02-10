package au.org.aekos.api.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StreamUtils;

import au.org.aekos.api.loader.service.load.IndexingService;
import au.org.aekos.api.loader.util.CoreDataAekosJenaModelFactory;

@SpringBootApplication
@PropertySource("classpath:/au/org/aekos/api/loader/aekos-api-loader.properties")
@PropertySource(value="file://${user.home}/aekos-api.properties", ignoreResourceNotFound=true)
public class Application implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	@Autowired
	private IndexingService indexingService;
	
	@Value("${lucene.index.path}")
	private String indexPath;
	
    public static void main(String[] args) {
    	//org.apache.jena.tdb.TDB.getContext().set(org.apache.jena.query.ARQ.symLogExec,true); // Uncomment to enable TDB/ARQ debugging output
        SpringApplication.run(Application.class, args);
    }
    
    @Override
	public void run(String... arg0) throws Exception {
		try {
    		String finishedMessage = indexingService.doIndexing();
    		logger.info(finishedMessage);
			logger.info("Wrote index to " + indexPath);
		} catch (Throwable e) {
			logger.error("Failed to load data", e);
		}
	}
    
    @Bean
    public Dataset coreDS(CoreDataAekosJenaModelFactory loader) {
    	return loader.getDatasetInstance();
    }
    
    @Bean
    public String citationDetailsQuery() throws IOException {
		return getSparqlQuery("citation-details.rq");
    }
    
    @Bean
    public String darwinCoreAndTraitsQuery() throws IOException {
		return getSparqlQuery("darwin-core-and-traits.rq");
    }
    
    @Bean
    public String environmentalVariablesQuery() throws IOException {
		return getSparqlQuery("environmental-variables.rq");
    }

	private String getSparqlQuery(String fileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/loader/sparql/" + fileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}
