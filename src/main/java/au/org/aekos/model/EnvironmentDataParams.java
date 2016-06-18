package au.org.aekos.model;

import java.util.List;

public class EnvironmentDataParams extends AbstractParams {
	private final List<String> speciesNames;
	private final List<String> envVarNames;

	public EnvironmentDataParams(int start, int rows, List<String> speciesNames, List<String> envVarNames) {
		super(start, rows);
		this.speciesNames = speciesNames;
		this.envVarNames = envVarNames;
	}

	public List<String> getSpeciesNames() {
		return speciesNames;
	}

	public List<String> getEnvVarNames() {
		return envVarNames;
	}
}