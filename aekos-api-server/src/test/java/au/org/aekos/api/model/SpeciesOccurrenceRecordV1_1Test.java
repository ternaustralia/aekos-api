package au.org.aekos.api.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SpeciesOccurrenceRecordV1_1Test {

	/**
	 * Can we serialise to a CSV row with a scientificName?
	 */
	@Test
	public void testToCsv01() {
		SpeciesOccurrenceRecordV1_1 objectUnderTest = new SpeciesOccurrenceRecordV1_1(-32.1094d, 139.3506d, "GDA94",
				"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", "Atriplex stipitata", 1, 
				"2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"pp28A", "Some Test Survey");
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\","
				+ "\"Atriplex stipitata\",\"\",1,\"2012-11-23\",2012,11,\"some citation stuff\","
				+ "\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"pp28A\",\"Some Test Survey\""));
	}
	
	/**
	 * Can we serialise to a CSV row with a taxonRemark?
	 */
	@Test
	public void testToCsv02() {
		SpeciesOccurrenceRecordV1_1 objectUnderTest = new SpeciesOccurrenceRecordV1_1(-32.1094d, 139.3506d, "GDA94", 
				"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", 1, "2012-11-23", 2012, 11,
				"some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", "Hakea obtusa",
				"pp28A", "Some Test Survey");
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"\","
				+ "\"Hakea obtusa\",1,\"2012-11-23\",2012,11,\"some citation stuff\","
				+ "\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"pp28A\",\"Some Test Survey\""));
	}
}
