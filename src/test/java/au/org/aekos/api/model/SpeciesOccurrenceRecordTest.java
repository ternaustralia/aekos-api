package au.org.aekos.api.model;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.junit.Test;

import au.org.aekos.api.model.SpeciesOccurrenceRecord;
import au.org.aekos.api.model.VisitInfo;

public class SpeciesOccurrenceRecordTest {

	/**
	 * Can we serialise to a CSV row with a scientificName?
	 */
	@Test
	public void testToCsv01() {
		SpeciesOccurrenceRecord objectUnderTest = new SpeciesOccurrenceRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",\"\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""));
	}
	
	/**
	 * Can we serialise to a CSV row with a taxonRemark?
	 */
	@Test
	public void testToCsv02() {
		SpeciesOccurrenceRecord objectUnderTest = new SpeciesOccurrenceRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", "Hakea obtusa");
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"\",\"Hakea obtusa\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""));
	}
	
	/**
	 * Can we append a scientificName?
	 */
	@Test
	public void testHasSpeciesName01() {
		SpeciesOccurrenceRecord objectUnderTest = new SpeciesOccurrenceRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		VisitInfo item = new VisitInfo("not important", "not important");
		objectUnderTest.appendSpeciesNameTo(item);
		Set<String> result = item.getScientificNames();
		assertThat(result, hasItems("Atriplex stipitata"));
		assertThat(item.getTaxonRemarks().size(), is(0));
	}
	
	/**
	 * Can we append a taxonRemark?
	 */
	@Test
	public void testHasSpeciesName02() {
		SpeciesOccurrenceRecord objectUnderTest = new SpeciesOccurrenceRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A", "Hakea obtusa");
		VisitInfo item = new VisitInfo("not important", "not important");
		objectUnderTest.appendSpeciesNameTo(item);
		Set<String> result = item.getTaxonRemarks();
		assertThat(result, hasItems("Hakea obtusa"));
		assertThat(item.getScientificNames().size(), is(0));
	}
}
