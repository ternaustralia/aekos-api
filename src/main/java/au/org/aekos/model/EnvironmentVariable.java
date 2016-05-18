package au.org.aekos.model;

public class EnvironmentVariable {

	private final String code;
	private final String label;

	public EnvironmentVariable(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}
}
