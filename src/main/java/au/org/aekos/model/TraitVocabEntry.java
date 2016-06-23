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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraitVocabEntry other = (TraitVocabEntry) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
	
}
