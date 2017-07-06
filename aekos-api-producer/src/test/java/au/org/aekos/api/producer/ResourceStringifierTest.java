package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ResourceStringifierTest {
	
	/**
	 * Can we get the value when the file exists?
	 */
	@Test
	public void testGetValue01() throws Throwable {
		ResourceStringifier objectUnderTest = new ResourceStringifier("au/org/aekos/api/producer/fileWithStuff.txt");
		String result = objectUnderTest.getValue();
		assertThat(result, is("Some stuff"));
	}
}
