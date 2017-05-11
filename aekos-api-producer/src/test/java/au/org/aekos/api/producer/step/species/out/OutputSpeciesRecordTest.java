package au.org.aekos.api.producer.step.species.out;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class OutputSpeciesRecordTest {

private OutputSpeciesRecord objectUnderTest;
	
	@Before
	public void before() {
		objectUnderTest = new OutputSpeciesRecord(new InputSpeciesRecord("abc-123", 1, "location123", "macropus rufus", "buffalo grass"));
	}
	
	/**
	 * Is the scientific name quoted?
	 */
	@Test
	public void testGetScientificName01() {
		String result = objectUnderTest.getScientificName();
		assertEquals("\"macropus rufus\"", result);
	}
	
	// TODO test all other fields
	
	/**
	 * Are all the declared field names present?
	 */
	@Test
	public void testGetCsvFields01() {
		String[] fields = OutputSpeciesRecord.getCsvFields();
		for (String curr : fields) {
			String methodName = "get" + StringUtils.capitalize(curr);
			try {
				OutputSpeciesRecord.class.getMethod(methodName);
				// success
			} catch (NoSuchMethodException e) {
				fail("Expected method to exist: " + methodName);
			}
		}
	}
}
