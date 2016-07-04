package au.org.aekos.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TraitDataRecordWrapperTest {

	/**
	 * Can we deserialise a String[] to an object when all fields are present?
	 */
	@Test
	public void testDeserialiseFrom01() {
		String citation = "Koonamore Research Group (2015). Vegetation Quadrats, Koonamore Vegetation Monitoring Project "
				+ "(1925-present), Version 12 /2014. Persistent Hyperlink: "
				+ "http://www.aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats. ÆKOS "
				+ "Data Portal, rights owned by The University of Adelaide (www.adelaide.edu.au). "
				+ "Accessed [dd mmm yyyy, e.g., 01 Jan 2016].";
		String[] fields = new String[] {
				"-32.10841","139.35191","aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2",
				"Senna artemisioides subsp. petiolaris","Individuals","23/11/12","2012","11","height","2",
				citation, "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2"
		};
		TraitDataRecordWrapper result = TraitDataRecordWrapper.deserialiseFrom(fields);
		assertEquals(-32.10841d, result.getDecimalLatitude(), 0.00001);
		assertEquals(139.35191d, result.getDecimalLongitude(), 0.00001);
		assertEquals("aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2", result.getLocationID());
		assertEquals("Senna artemisioides subsp. petiolaris", result.getScientificName());
		assertEquals("Individuals", result.getCollectionFormat());
		assertEquals("23/11/12", result.getEventDate());
		assertEquals(2012, result.getYear());
		assertEquals(11, result.getMonth());
		assertEquals("height", result.getTrait());
		assertEquals("2", result.getTraitValue());
		assertEquals(citation, result.getBibliographicCitation());
		assertEquals("http://aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2", result.getSamplingProtocol());
	}
}
