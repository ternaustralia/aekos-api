package au.org.aekos.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpeciesOccurrenceRecordTest {

	/**
	 * Can we deserialise a String[] to an object when all fields are present?
	 */
	@Test
	public void testDeserialiseFrom01() {
		String citation = "Koonamore Research Group (2015). Vegetation Quadrats, Koonamore Vegetation Monitoring Project (1925-present), "
				+ "Version 12 /2014. Persistent Hyperlink: http://www.aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats. "
				+ "ÆKOS Data Portal, rights owned by The University of Adelaide (www.adelaide.edu.au). Accessed [dd mmm yyyy e.g., 01 Jan 2016].";
		String[] fields = new String[] {
				"-32.1094","139.3506","GDA94","aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A","Atriplex stipitata","1",
				"2012-11-23","2012","11",citation, "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A"
		};
		SpeciesOccurrenceRecord result = SpeciesOccurrenceRecord.deserialiseFrom(fields);
		assertEquals(-32.1094d, result.getDecimalLatitude(), 0.00001);
		assertEquals(139.3506d, result.getDecimalLongitude(), 0.00001);
		assertEquals("GDA94", result.getGeodeticDatum());
		assertEquals("aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", result.getLocationID());
		assertEquals("Atriplex stipitata", result.getScientificName());
		assertEquals(1, result.getIndividualCount());
		assertEquals("2012-11-23", result.getEventDate());
		assertEquals(2012, result.getYear());
		assertEquals(11, result.getMonth());
		assertEquals(citation, result.getBibliographicCitation());
		assertEquals("http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", result.getDatasetID());
	}
	
	/**
	 * Can we serialise to a CSV row?
	 */
	@Test
	public void testToCsv01() {
		SpeciesOccurrenceRecord objectUnderTest = new SpeciesOccurrenceRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""));
	}
}
