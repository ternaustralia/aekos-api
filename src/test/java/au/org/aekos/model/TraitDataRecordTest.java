package au.org.aekos.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import au.org.aekos.model.TraitDataRecord.Entry;

public class TraitDataRecordTest {

	/**
	 * Can we transform to a CSV record when there's only one trait?
	 */
	@Test
	public void testToCsv01() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A");
		objectUnderTest.addTraitValue(new Entry("growthForm", "Tree", ""));
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",1"
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
		objectUnderTest.addTraitValue(new Entry("growthForm", "Tree", ""));
		objectUnderTest.addTraitValue(new Entry("growthForm", "Shrub", ""));
		String result = objectUnderTest.toCsv();
		assertThat(result, is("-32.1094,139.3506,\"GDA94\",\"aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\",\"Atriplex stipitata\",1"
				+ ",\"2012-11-23\",2012,11,\"some citation stuff\",\"http://aekos.org.au/collection/adelaide.edu.au/Koonamore/Photopoints/pp28A\""
				+ ",\"growthForm\",\"Tree\",\"\",\"growthForm\",\"Shrub\",\"\""));
	}
	
	/**
	 * Does a record match an empty trait filter?
	 */
	@Test
	public void testMatchesTraitFilter01() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://Photopoints/pp28A");
		List<String> traitNames = Collections.emptyList();
		boolean result = objectUnderTest.matchesTraitFilter(traitNames);
		assertTrue("anything should match an empty filter", result);
	}
	
	/**
	 * Does a record match a filter when it has one of the traits?
	 */
	@Test
	public void testMatchesTraitFilter02() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://Photopoints/pp28A");
		objectUnderTest.addTraitValue(new Entry("growthForm", "Tree", ""));
		objectUnderTest.addTraitValue(new Entry("lifeStage", "Early", ""));
		List<String> traitNames = Arrays.asList("lifeStage");
		boolean result = objectUnderTest.matchesTraitFilter(traitNames);
		assertTrue("lifeStage is a present trait", result);
	}
	
	/**
	 * Does a record fail to match a filter when it doesn't have one of the traits?
	 */
	@Test
	public void testMatchesTraitFilter03() {
		TraitDataRecord objectUnderTest = new TraitDataRecord(-32.1094d, 139.3506d, "GDA94", "/Photopoints/pp28A",
				"Atriplex stipitata", 1, "2012-11-23", 2012, 11, "some citation stuff", "http://Photopoints/pp28A");
		objectUnderTest.addTraitValue(new Entry("growthForm", "Tree", ""));
		objectUnderTest.addTraitValue(new Entry("lifeStage", "Early", ""));
		List<String> traitNames = Arrays.asList("dominance");
		boolean result = objectUnderTest.matchesTraitFilter(traitNames);
		assertFalse("dominance is NOT a present trait", result);
	}
}
