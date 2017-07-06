package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class CleanQuotedValueTest {

	/**
	 * Can we quote a value?
	 */
	@Test
	public void testQuote01() {
		CleanQuotedValue objectUnderTest = new CleanQuotedValue("blah");
		String result = objectUnderTest.getValue();
		assertThat(result, is("\"blah\""));
	}
	
	/**
	 * Is null output as the correct escape sequence?
	 */
	@Test
	public void testQuote02() {
		CleanQuotedValue objectUnderTest = new CleanQuotedValue(null);
		String result = objectUnderTest.getValue();
		assertThat(result, is("\\N"));
	}
	
	/**
	 * Can we clean a value that would otherwise break things?
	 */
	@Test
	public void testQuote03() {
		CleanQuotedValue objectUnderTest = new CleanQuotedValue("\"(\"\"Epacrid ?\"\")\"");
		String result = objectUnderTest.getValue();
		assertThat(result, is("\"(Epacrid ?)\""));
	}
}
