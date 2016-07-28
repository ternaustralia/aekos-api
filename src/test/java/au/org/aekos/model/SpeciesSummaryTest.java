package au.org.aekos.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpeciesSummaryTest {

	/**
	 * Are two species with the same name but different count still equal?
	 */
	@Test
	public void testEquals01() {
		SpeciesSummary one = new SpeciesSummary("Leersia hexandra", 1);
		SpeciesSummary two = new SpeciesSummary("Leersia hexandra", 2);
		assertEquals(one, two);
	}
}
