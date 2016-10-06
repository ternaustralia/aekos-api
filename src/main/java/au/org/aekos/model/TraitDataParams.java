package au.org.aekos.model;

import java.util.List;

public class TraitDataParams extends SpeciesDataParams {
	private final List<String> traitNames;

	public TraitDataParams(int start, int rows, List<String> speciesNames, List<String> traitNames) {
		super(start, rows, speciesNames);
		this.traitNames = traitNames;
	}

	public List<String> getTraitNames() {
		return traitNames;
	}
}