package au.org.aekos.api.producer.out;

import static org.junit.Assert.*;

import org.junit.Test;

import au.org.aekos.api.producer.step.species.out.SpeciesRecord;

public class SpeciesRecordTest {

	/**
	 * Is the species name quoted?
	 */
	@Test
	public void testGetSpeciesName() {
		SpeciesRecord objectUnderTest = new SpeciesRecord(123, "species1");
		String result = objectUnderTest.getSpeciesName();
		assertEquals("\"species1\"", result);
	}
}
