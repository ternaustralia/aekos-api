package au.org.aekos.api.model;

import java.util.List;

public class EnvironmentDataParams extends SpeciesDataParams {
	private final List<String> envVarNames;

	public EnvironmentDataParams(int start, int rows, List<String> speciesNames, List<String> envVarNames) {
		super(start, rows, speciesNames);
		this.envVarNames = envVarNames;
	}

	public List<String> getEnvVarNames() {
		return envVarNames;
	}
}