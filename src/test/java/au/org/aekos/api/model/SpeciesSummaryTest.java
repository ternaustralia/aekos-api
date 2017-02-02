package au.org.aekos.api.model;

import static org.junit.Assert.*;

import org.junit.Test;

import au.org.aekos.api.model.SpeciesSummary;

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
