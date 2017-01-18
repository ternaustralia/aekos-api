package au.org.aekos.util;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CoreDataAekosJenaModelFactory extends AbstractAekosJenaModelFactory {

	@Value("${aekos-api.core-data-model-name}")
	private String modelName;
	
	@Value("${aekos-api.core-data-tdb-dir}")
	private String modelPath;
	
	@Override
	String getModelName() {
		return modelName;
	}

	@Override
	void doPostConstructStats(Model newInstance) {
		// do nothing
	}
	
	@Override
	String getModelPath() {
		return modelPath;
	}
}
