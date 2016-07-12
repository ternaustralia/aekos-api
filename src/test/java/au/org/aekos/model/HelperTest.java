package au.org.aekos.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class HelperTest {

	/**
	 * Does a record match an empty trait filter?
	 */
	@Test
	public void testMatchesTraitFilter01() {
		Collection<TraitOrEnvironmentalVariable> traits = new ArrayList<>();
		traits.add(new TraitOrEnvironmentalVariable("growthForm", "Tree", ""));
		traits.add(new TraitOrEnvironmentalVariable("lifeStage", "Early", ""));
		List<String> traitNames = Collections.emptyList();
		boolean result = Helper.matchesFilter(traitNames, traits);
		assertTrue("anything should match an empty filter", result);
	}
	
	/**
	 * Does a record match a filter when it has one of the traits?
	 */
	@Test
	public void testMatchesTraitFilter02() {
		Collection<TraitOrEnvironmentalVariable> traits = new ArrayList<>();
		traits.add(new TraitOrEnvironmentalVariable("growthForm", "Tree", ""));
		traits.add(new TraitOrEnvironmentalVariable("lifeStage", "Early", ""));
		List<String> traitNames = Arrays.asList("lifeStage");
		boolean result = Helper.matchesFilter(traitNames, traits);
		assertTrue("lifeStage is a present trait", result);
	}
	
	/**
	 * Does a record fail to match a filter when it doesn't have one of the traits?
	 */
	@Test
	public void testMatchesTraitFilter03() {
		Collection<TraitOrEnvironmentalVariable> traits = new ArrayList<>();
		traits.add(new TraitOrEnvironmentalVariable("growthForm", "Tree", ""));
		traits.add(new TraitOrEnvironmentalVariable("lifeStage", "Early", ""));
		List<String> traitNames = Arrays.asList("dominance");
		boolean result = Helper.matchesFilter(traitNames, traits);
		assertFalse("dominance is NOT a present trait", result);
	}
}
