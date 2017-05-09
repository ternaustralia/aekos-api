package au.org.aekos.api.producer;

public class Utils {

	private Utils() {}
	
	public static String quote(String value) {
		return "\"" + value + "\"";
	}
}
