package au.org.aekos.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpeciesDataRecordTest {

	@Test
	public void test() {
		String[] fields = new String[] {
				"-32.10841","139.35191","aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2",
				"Senna artemisioides subsp. petiolaris","Individuals","23/11/12","2012","11","200","Scrub","9.1","0.28",
				"Koonamore Research Group (2015). Kangaroo Transects, Koonamore Vegetation Monitoring Project (1925-present), Version 12 /2014. Persistent Hyperlink: http://www.aekos.org.au/collection/adelaide.edu.au/Koonamore/KangarooTransects. AEKOS Data Portal, rights owned by The University of Adelaide (www.adelaide.edu.au). Accessed [importDate].",
				"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/VegetationQuadrats/QFR2"
		};
		SpeciesDataRecord result = SpeciesDataRecord.deserialiseFrom(fields);
		assertEquals(-32.10841d, result.getLatCoord(), 0.00001);
		// TODO add the other asserts
	}
}
