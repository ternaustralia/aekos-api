package au.org.aekos.api.producer.step.species.in;

import java.util.Collections;
import java.util.List;

public class InputSpeciesRecord {
	private final String speciesName;
	private final List<Trait> traits;
	
	public InputSpeciesRecord(String speciesName, List<Trait> traits) {
		this.speciesName = speciesName;
		this.traits = traits;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public List<Trait> getTraits() {
		return Collections.unmodifiableList(traits);
	}
}
