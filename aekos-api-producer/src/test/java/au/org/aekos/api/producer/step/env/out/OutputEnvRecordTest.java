package au.org.aekos.api.producer.step.env.out;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.util.StringUtils;

public class OutputEnvRecordTest {

	/**
	 * Are all the declared field names present?
	 */
	@Test
	public void testGetCsvFields01() {
		String[] fields = OutputEnvRecord.getCsvFields();
		for (String curr : fields) {
			String methodName = "get" + StringUtils.capitalize(curr);
			try {
				OutputEnvRecord.class.getMethod(methodName);
				// success
			} catch (NoSuchMethodException e) {
				fail("Expected method to exist: " + methodName);
			}
		}
	}
}
