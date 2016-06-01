package au.org.aekos.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvironmentDataRecordTest {

	/**
	 * Can we deserialise a String[] to an object when all fields are present?
	 */
	@Test
	public void testDeserialiseFrom01() {
		String citation = "Koonamore Research Group (2015). Kangaroo Transects, Koonamore Vegetation Monitoring Project "
				+ "(1925-present), Version 12 /2014. Persistent Hyperlink: http://www.aekos.org.au/collection/adelaide.edu.au/Koonamore/KangarooTransects. "
				+ "ÆKOS Data Portal, rights owned by The University of Adelaide (www.adelaide.edu.au). Accessed [importDate].";
		String[] fields = new String[] {
				"-32.10841", "139.35191", "aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2", 
				"23/11/12", "2012", "11", "soilPh_10cm", "4.5", citation,
				"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2"
		};
		EnvironmentDataRecord result = EnvironmentDataRecord.deserialiseFrom(fields);
		assertEquals(-32.10841d, result.getDecimalLatitude(), 0.00001);
		assertEquals(139.35191d, result.getDecimalLongitude(), 0.00001);
		assertEquals("aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2", result.getLocationID());
		assertEquals("23/11/12", result.getEventDate());
		assertEquals("2012", result.getYear());
		assertEquals("11", result.getMonth());
		assertEquals("soilPh_10cm", result.getEnvVariable());
		assertEquals("4.5", result.getEnvVariableValue());
		assertEquals(citation, result.getBibliographicCitation());
		assertEquals("http://aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2", result.getDatasetID());
	}

}
