package au.org.aekos.util;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MetricsAekosJenaModelFactory extends AbstractAekosJenaModelFactory {

	private static final Logger logger = LoggerFactory.getLogger(MetricsAekosJenaModelFactory.class);

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
		countExistingMetrics(newInstance);
	}

	@Override
	String getModelPath() {
		return modelPath;
	}
	
	private void countExistingMetrics(Model newInstance) {
		logger.info(getModelName() + " metricRecord store has " + newInstance.listSubjects().toList().size() + " existing metric(s).");
	}
}
