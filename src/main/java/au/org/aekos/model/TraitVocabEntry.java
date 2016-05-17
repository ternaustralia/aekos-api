package au.org.aekos.model;

public class TraitVocabEntry {

	private final String code;
	private final String label;

	public TraitVocabEntry(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}


	public String getLabel() {
		return label;
	}

	public static TraitVocabEntry deserialiseFrom(String[] csvArray) {
		return new TraitVocabEntry(csvArray[0], csvArray[1]);
	}
}
