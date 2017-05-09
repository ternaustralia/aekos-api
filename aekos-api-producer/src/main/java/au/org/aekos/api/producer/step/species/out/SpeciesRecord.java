package au.org.aekos.api.producer.step.species.out;

import au.org.aekos.api.producer.Utils;

public class SpeciesRecord {
	private final int id;
    private final String speciesName;

    public SpeciesRecord(int id, String speciesName) {
		this.id = id;
		this.speciesName = speciesName;
	}

	public int getId() {
		return id;
	}

	public String getSpeciesName() {
		return Utils.quote(speciesName);
	}

	public static String[] getCsvFields() {
		return new String[] {"id", "speciesName"};
	}
}
