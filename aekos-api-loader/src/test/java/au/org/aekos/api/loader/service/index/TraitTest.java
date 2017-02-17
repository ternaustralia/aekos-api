package au.org.aekos.api.loader.service.index;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class TraitTest {

	/**
	 * Can we tell when we don't have units?
	 */
	@Test
	public void testHasUnits01() {
		Trait objectUnderTest = new Trait("lifeForm", "Shrub", "");
		boolean result = objectUnderTest.hasUnits();
		assertFalse(result);
	}
	
	/**
	 * Can we tell when we do have units?
	 */
	@Test
	public void testHasUnits02() {
		Trait objectUnderTest = new Trait("height", "3", "metres");
		boolean result = objectUnderTest.hasUnits();
		assertTrue(result);
	}
	
	/**
	 * Can we transform to JSON?
	 */
	@Test
	public void testToJson01() {
		Trait objectUnderTest = new Trait("height", "3", "metres");
		String result = objectUnderTest.toJson();
		assertThat(result, is("{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"}"));
	}
	
	/**
	 * Can we transform to JSON when we have no units?
	 */
	@Test
	public void testToJson02() {
		Trait objectUnderTest = new Trait("lifeForm", "Shrub", "");
		String result = objectUnderTest.toJson();
		assertThat(result, is("{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}"));
	}
	
	/**
	 * Can we transform from JSON?
	 */
	@Test
	public void testfromJson01() {
		Trait result = Trait.fromJson("{\"name\":\"height\",\"value\":\"3\",\"units\":\"metres\"}");
		assertThat(result.getName(), is("height"));
		assertThat(result.getValue(), is("3"));
		assertThat(result.getUnits(), is("metres"));
	}
	
	/**
	 * Can we transform from JSON with no units?
	 */
	@Test
	public void testfromJson02() {
		Trait result = Trait.fromJson("{\"name\":\"lifeForm\",\"value\":\"Shrub\",\"units\":\"\"}");
		assertThat(result.getName(), is("lifeForm"));
		assertThat(result.getValue(), is("Shrub"));
		assertFalse(result.hasUnits());
	}
}
