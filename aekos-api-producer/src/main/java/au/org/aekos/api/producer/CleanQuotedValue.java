package au.org.aekos.api.producer;

public class CleanQuotedValue {
	public static final String MYSQL_NULL = "\\N";
	
	private final String rawValue;

	public CleanQuotedValue(String rawValue) {
		this.rawValue = rawValue;
	}
	
	public String getValue() {
		if (rawValue == null) {
			return MYSQL_NULL;
		}
		String cleanedValue = rawValue.replace("\"", ""); // Not sure if this is the right thing to do, but it works for now
		return "\"" + cleanedValue + "\"";
	}
}
