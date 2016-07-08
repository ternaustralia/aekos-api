package au.org.aekos.service.auth;

import javax.annotation.PreDestroy;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JenaAuthModelFactory implements AuthModelFactory {

	private static final Logger logger = LoggerFactory.getLogger(JenaAuthModelFactory.class);
	
	@Value("${aekos-api.auth-tdb-dir}")
	private String authModelPath;
	
	@Value("${aekos-api.not-defined}")
	private String notDefined;
	
	private Model instance;
	private Dataset dataset;
	
	@Override
	public Model getInstance() {
		if (instance != null) {
			return instance;
		}
		if (notDefined.equals(authModelPath)) {
			logger.info("Using in-memory auth model");
			instance = ModelFactory.createDefaultModel();
			return instance;
		}
		logger.info("Using disk based memory auth model at " + authModelPath);
		dataset = TDBFactory.createDataset(authModelPath);
		instance = dataset.getDefaultModel();
		countExistingKeys();
		return instance;
	}
	
	private void countExistingKeys() {
		logger.info("Auth key store has " + instance.listSubjects().toList().size() + " existing key(s).");
	}

	@PreDestroy
	public void destroy() {
		if (instance != null) {
			instance.close();
		}
		if (dataset != null) {
			dataset.close();
		}
	}
}
