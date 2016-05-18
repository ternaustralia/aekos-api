package au.org.aekos.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpeciesNameTest {

	/**
	 * Do two species that are logically equal have the same ID?
	 */
	@Test
	public void testGetIdentifier01() {
		SpeciesName one = new SpeciesName("Leersia hexandra");
		SpeciesName two = new SpeciesName("Leersia hexandra");
		assertEquals(one.getId(), two.getId());
	}
}
