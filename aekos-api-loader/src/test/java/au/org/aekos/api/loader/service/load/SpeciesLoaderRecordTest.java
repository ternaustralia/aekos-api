package au.org.aekos.api.loader.service.load;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class SpeciesLoaderRecordTest {

	/**
	 * Is the join key equal to the one generated from the environment record?
	 */
	@Test
	public void testGetJoinKey01() {
		String locationID = "loc1";
		String eventDate = "2016-03-20";
		SpeciesLoaderRecord objectUnderTest = new SpeciesLoaderRecord("species1", Collections.emptySet(),
				"not important", "not important", locationID, eventDate);
		EnvironmentLoaderRecord envRecord = new EnvironmentLoaderRecord(locationID, Collections.emptySet(), eventDate);
		String result = objectUnderTest.getJoinKey();
		String otherJoinKey = envRecord.getJoinKey();
		assertTrue("Must be equal", result.equals(otherJoinKey));
	}

}
