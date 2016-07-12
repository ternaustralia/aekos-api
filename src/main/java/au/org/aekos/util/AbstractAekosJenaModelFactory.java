package au.org.aekos.util;

import javax.annotation.PreDestroy;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractAekosJenaModelFactory implements AekosJenaModelFactory {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAekosJenaModelFactory.class);
	
	@Value("${aekos-api.not-defined}")
	private String notDefined;
	
	@Value("${aekos-api.is-production}")
	private boolean isProd;
	
	private Model instance;
	private Dataset dataset;
	
	@Override
	public Model getInstance() {
		if (instance != null) {
			return instance;
		}
		if (notDefined.equals(getModelPath())) {
			if (isProd) {
				throw new IllegalStateException("Config problem: no " + getModelName() + " TDB data directory defined. "
						+ "Cannot continue without it. Either supply it or turn production mode off.");
			}
			logger.info("Using in-memory " + getModelName() + " model");
			instance = ModelFactory.createDefaultModel();
			return instance;
		}
		logger.info("Using disk based " + getModelName() + " model at " + getModelPath());
		dataset = TDBFactory.createDataset(getModelPath());
		instance = dataset.getDefaultModel();
		doPostConstructStats(instance);
		return instance;
	}
	
	abstract String getModelPath();

	abstract String getModelName();
	
	abstract void doPostConstructStats(Model newInstance);
	
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
