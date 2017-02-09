package au.org.aekos.api.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import au.org.aekos.api.model.TraitDataRecord;
import au.org.aekos.api.model.TraitOrEnvironmentalVariable;

public class TraitDataRecordTest {

	/**
	 * Can we transform to a CSV record when there's only one trait?
	 */
	@Test
	public void testToCsv01() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		objectUnderTest.addTraitValue(new TraitOrEnvironmentalVariable("growthForm", "Tree", ""));
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",\"\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""
				+ ",\"growthForm\",\"Tree\",\"\""));
	}

	/**
	 * Can we transform to a CSV record when there's more than one trait?
	 */
	@Test
	public void testToCsv02() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		objectUnderTest.addTraitValue(new TraitOrEnvironmentalVariable("growthForm", "Tree", ""));
		objectUnderTest.addTraitValue(new TraitOrEnvironmentalVariable("growthForm", "Shrub", ""));
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",\"\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""
				+ ",\"growthForm\",\"Tree\",\"\",\"growthForm\",\"Shrub\",\"\""));
	}
}
