package au.org.aekos.api.producer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.StreamUtils;

public class Utils {

	private Utils() {}
	
	public static String quote(String value) {
		return "\"" + value + "\"";
	}
	
	/**
	 * @param classpathAndFileName	something like "au/org/aekos/api/producer/sparql/" + fileName
	 * @return	contents of the file as a String
	 */
	public static String getResourceAsString(String classpathAndFileName) throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathAndFileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}
