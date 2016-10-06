package au.org.aekos.model;

import java.util.List;

public class SpeciesDataParams extends AbstractParams {
	private final List<String> speciesNames;

	public SpeciesDataParams(int start, int rows, List<String> speciesNames) {
		super(start, rows);
		this.speciesNames = speciesNames;
	}

	public List<String> getSpeciesNames() {
		return speciesNames;
	}
}