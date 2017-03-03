package au.org.aekos.api.loader.util;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class AuthAekosJenaModelFactory extends AbstractAekosJenaModelFactory {

	private static final Logger logger = LoggerFactory.getLogger(AuthAekosJenaModelFactory.class);
	
	@Value("${aekos-api.auth-model-name}")
	private String modelName;
	
	@Value("${aekos-api.auth-tdb-dir}")
	private String modelPath;
	
	@Override
	String getModelName() {
		return modelName;
	}

	@Override
	void doPostConstructStats(Model newInstance) {
		countExistingKeys(newInstance);
	}
	
	@Override
	String getModelPath() {
		return modelPath;
	}
	
	private void countExistingKeys(Model newInstance) {
		logger.info(getModelName() + " key store has " + newInstance.listSubjects().toList().size() + " existing key(s).");
	}
}
