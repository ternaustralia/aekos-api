package au.org.aekos.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class ControllerHelperTest {

	private static final int MARCH = 2;

	/**
	 * Can we generate a download file name?
	 */
	@Test
	public void testGenerateDownloadFileName01() {
		String dataTypeName = "species";
		Date when = new GregorianCalendar(2016,MARCH,10).getTime();
		String result = ControllerHelper.generateDownloadFileName(dataTypeName, when);
		assertThat(result, is("aekos-api-species-data-20160310.csv"));
	}

}
