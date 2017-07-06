package au.org.aekos.api.producer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.StreamUtils;

public class ResourceStringifier {
	private final String classpathAndFileName;

	/**
	 * @param classpathAndFileName	something like "au/org/aekos/api/producer/sparql/" + fileName
	 */
	public ResourceStringifier(String classpathAndFileName) {
		this.classpathAndFileName = classpathAndFileName;
	}
	
	/**
	 * @return	contents of the resource (file) as a String
	 */
	public String getValue() throws IOException {
		InputStream sparqlIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathAndFileName);
		OutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(sparqlIS, out);
		return out.toString();
	}
}