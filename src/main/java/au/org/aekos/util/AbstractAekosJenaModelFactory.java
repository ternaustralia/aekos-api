package au.org.aekos.util;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
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
	protected Dataset dataset;
	
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
			dataset = DatasetFactory.create();
			instance = dataset.getDefaultModel();
			logTransactionSupport();
			return instance;
		}
		logger.info("Using disk based " + getModelName() + " model at " + getModelPath());
		dataset = TDBFactory.createDataset(getModelPath());
		instance = dataset.getDefaultModel();
		logTransactionSupport();
		doPostConstructStats(instance);
		return instance;
	}
	
	private void logTransactionSupport() {
		String transactionSupportFragment = dataset.supportsTransactions() ? "DOES" : "DOES NOT";
		logger.warn(String.format("The %s model %s support transactions.", getModelName(), transactionSupportFragment));
	}

	@Override
	public Dataset getDatasetInstance() {
		getInstance();
		return dataset;
	}
	
	abstract String getModelPath();

	abstract String getModelName();
	
	abstract void doPostConstructStats(Model newInstance);
}
