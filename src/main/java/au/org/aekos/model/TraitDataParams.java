package au.org.aekos.model;

import java.util.List;

public class TraitDataParams extends AbstractParams {
	private final List<String> speciesNames;
	private final List<String> traitNames;

	public TraitDataParams(int start, int rows, List<String> speciesNames, List<String> traitNames) {
		super(start, rows);
		this.speciesNames = speciesNames;
		this.traitNames = traitNames;
	}

	public List<String> getSpeciesNames() {
		return speciesNames;
	}

	public List<String> getTraitNames() {
		return traitNames;
	}
}