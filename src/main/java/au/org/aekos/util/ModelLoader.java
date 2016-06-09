package au.org.aekos.util;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelLoader {

	private static final Logger logger = LoggerFactory.getLogger(ModelLoader.class);
	
	@Value("${aekos-api.tdb-dir}")
	private String tdbDataDir;
	
	@Value("${aekos-api.is-production}")
	private boolean isProd;
	
	@Value("${aekos-api.not-defined}")
	private String notDefinedFlag;
	
	public Model loadModel() {
		if (tdbDataDir == null || tdbDataDir.equals(notDefinedFlag)) {
			if (isProd) {
				throw new IllegalStateException("Config problem: no TDB data directory defined. "
						+ "Cannot continue without it. Either supply it or turn production mode off.");
			}
			logger.warn(" _     _  _______  ______    __    _  ___   __    _  _______ ");
			logger.warn("| | _ | ||   _   ||    _ |  |  |  | ||   | |  |  | ||       |");
			logger.warn("| || || ||  |_|  ||   | ||  |   |_| ||   | |   |_| ||    ___|");
			logger.warn("|       ||       ||   |_||_ |       ||   | |       ||   | __ ");
			logger.warn("|       ||       ||    __  ||  _    ||   | |  _    ||   ||  |");
			logger.warn("|   _   ||   _   ||   |  | || | |   ||   | | | |   ||   |_| |");
			logger.warn("|__| |__||__| |__||___|  |_||_|  |__||___| |_|  |__||_______|");
			logger.warn("No TDB data directory defined so using an empty, in-memory instance.");
			return ModelFactory.createDefaultModel();
		}
		Dataset dataset = TDBFactory.createDataset(tdbDataDir);
		Model result = dataset.getDefaultModel();
		logger.info("Using TDB data directory at " + tdbDataDir);
		return result;
	}

	public void setTdbDataDir(String tdbDataDir) {
		this.tdbDataDir = tdbDataDir;
	}

	public void setProd(boolean isProd) {
		this.isProd = isProd;
	}

	public void setNotDefinedFlag(String notDefinedFlag) {
		this.notDefinedFlag = notDefinedFlag;
	}
}
