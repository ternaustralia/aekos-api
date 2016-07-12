package au.org.aekos;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.FileCopyUtils;

import au.org.aekos.service.metric.JenaMetricsStorageServiceTest;

public class TestUtils {

	private TestUtils() { }
	
	public static String loadRetrieval(String filename) {
		return load("retrieval/" + filename);
	}
	
	public static String loadAuth(String filename) {
		return load("auth/" + filename);
	}
	
	public static String loadMetric(String filename) {
		return load("metric/" + filename);
	}
	
	private static String load(String filename) {
		InputStream in = JenaMetricsStorageServiceTest.class.getClassLoader().getResourceAsStream("au/org/aekos/" + filename);
		try {
			return new String(FileCopyUtils.copyToByteArray(in));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load " + filename, e);
		}
	}
}
