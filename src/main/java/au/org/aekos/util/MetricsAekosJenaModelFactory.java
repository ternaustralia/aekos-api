package au.org.aekos.util;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MetricsAekosJenaModelFactory extends AbstractAekosJenaModelFactory {

	@Value("${aekos-api.metrics-model-name}")
	private String modelName;
	
	@Value("${aekos-api.metrics-tdb-dir}")
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
