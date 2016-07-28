package au.org.aekos.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpeciesNameTest {

	/**
	 * Are two species with the same name but different count still equal?
	 */
	@Test
	public void testEquals01() {
		SpeciesName one = new SpeciesName("Leersia hexandra");
		SpeciesName two = new SpeciesName("Leersia hexandra");
		assertEquals(one, two);
	}
}
