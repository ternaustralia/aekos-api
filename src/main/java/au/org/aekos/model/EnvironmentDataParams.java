package au.org.aekos.model;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import au.org.aekos.service.metric.JenaMetricsStorageService;

public class EnvironmentDataParams extends SpeciesDataParams {
	private static final String ENV_VARS_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramEnvVars";
	
	private final List<String> envVarNames;

	public EnvironmentDataParams(int start, int rows, List<String> speciesNames, List<String> envVarNames) {
		super(start, rows, speciesNames);
		this.envVarNames = envVarNames;
	}

	public List<String> getEnvVarNames() {
		return envVarNames;
	}

	@Override
	void subAppendTo(Resource subject, Model metricsModel) {
		super.subAppendTo(subject, metricsModel);
		Property startProp = metricsModel.createProperty(ENV_VARS_PROP);
		for (String curr : envVarNames) {
			metricsModel.add(subject, startProp, curr);
		}
	}
}