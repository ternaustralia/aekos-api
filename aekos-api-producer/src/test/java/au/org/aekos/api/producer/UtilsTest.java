package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class UtilsTest {

	/**
	 * Can we quote a value?
	 */
	@Test
	public void testQuote01() {
		String result = Utils.quote("blah");
		assertThat(result, is("\"blah\""));
	}
	
	/**
	 * Is null output as the correct escape sequence?
	 */
	@Test
	public void testQuote02() {
		String result = Utils.quote(null);
		assertThat(result, is("\\N"));
	}
}
