package au.org.aekos.api.producer.step.env.out;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.util.StringUtils;

public class EnvVarRecordTest {

	// TODO test all fields are quoted
	
	/**
	 * Are all the declared field names present?
	 */
	@Test
	public void testGetCsvFields01() {
		String[] fields = EnvVarRecord.getCsvFields();
		for (String curr : fields) {
			String methodName = "get" + StringUtils.capitalize(curr);
			try {
				EnvVarRecord.class.getMethod(methodName);
				// success
			} catch (NoSuchMethodException e) {
				fail("Expected method to exist: " + methodName);
			}
		}
	}
}
