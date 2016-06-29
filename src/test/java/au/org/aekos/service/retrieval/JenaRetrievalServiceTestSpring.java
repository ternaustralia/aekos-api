package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentDataRecord.Entry;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.SpeciesDataResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/retrievalContext-test.xml")
public class JenaRetrievalServiceTestSpring {

	@Autowired
	private JenaRetrievalService objectUnderTest;
	
	@Autowired
	@Qualifier("testGetSpeciesDataCsv01_expected")
	private String testGetSpeciesDataCsv01_expected;

	@Autowired
	@Qualifier("testGetSpeciesDataCsv02_expected")
	private String testGetSpeciesDataCsv02_expected;

	@Autowired
	@Qualifier("testGetSpeciesDataCsv03_expected")
	private String testGetSpeciesDataCsv03_expected;

	@Autowired
	@Qualifier("testGetEnvironmentalDataCsv01_expected")
	private String testGetEnvironmentalDataCsv01_expected;
	
	/**
	 * Can we get all the records that are available for the specified species?
	 */
	@Test
	public void testGetSpeciesDataJson01() throws Throwable {
		SpeciesDataResponse result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula"), 0, 10);
		assertThat(result.getResponse().size(), is(2));
	}

	/**
	 * Can we get all the records that are available for multiple species?
	 */
	@Test
	public void testGetSpeciesDataJson02() throws Throwable {
		SpeciesDataResponse result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula", "Goodenia havilandii"), 0, 10);
		assertThat(result.getResponse().size(), is(3));
	}

	/**
	 * Can we limit the records that we get for the specified species?
	 */
	@Test
	public void testGetSpeciesDataJson03() throws Throwable {
		int onlyOne = 1;
		SpeciesDataResponse result = objectUnderTest.getSpeciesDataJson(Arrays.asList("Calotis hispidula"), 0, onlyOne);
		assertThat(result.getResponse().size(), is(1));
	}

	/**
	 * Can we get all the records that are available for the specified species?
	 */
	@Test
	public void testGetSpeciesDataCsv01() throws Throwable {
		Writer writer = new StringWriter();
		objectUnderTest.getSpeciesDataCsv(Arrays.asList("Calotis hispidula"), 0, 20, writer);
		String compareStr = testGetSpeciesDataCsv01_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetSpeciesDataCsv01_expected.replaceAll("\r", "");
		}
		assertThat(writer.toString(), is(compareStr));
	}

	/**
	 * Can we get all the records that are available for multiple species?
	 */
	@Test
	public void testGetSpeciesDataCsv02() throws Throwable {
		Writer writer = new StringWriter();
		objectUnderTest.getSpeciesDataCsv(Arrays.asList("Calotis hispidula", "Goodenia havilandii"), 0, 20, writer);
		String compareStr = testGetSpeciesDataCsv02_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetSpeciesDataCsv02_expected.replaceAll("\r", "");
		}
		assertThat(writer.toString(), is(compareStr));
	}

	/**
	 * Can we limit the records that we get for the specified species?
	 */
	@Test
	public void testGetSpeciesDataCsv03() throws Throwable {
		int onlyOne = 1;
		Writer writer = new StringWriter();
		objectUnderTest.getSpeciesDataCsv(Arrays.asList("Calotis hispidula"), 0, onlyOne, writer);
		String compareStr = testGetSpeciesDataCsv03_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetSpeciesDataCsv03_expected.replaceAll("\r", "");
		}
		assertThat(writer.toString(), is(compareStr));
	}
	
	/**
	 * Can we get all the environmental data records that are available for the specified species?
	 */
	@Test
	public void testGetEnvironmentalDataCsv01() throws Throwable {
		Writer writer = new StringWriter();
		objectUnderTest.getEnvironmentalDataCsv(Arrays.asList("Calotis hispidula"), Collections.emptyList(), 0, 20, writer);
		String compareStr = testGetEnvironmentalDataCsv01_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetEnvironmentalDataCsv01_expected.replaceAll("\r", System.lineSeparator());
		}
		assertEquals(compareStr, writer.toString());
		// FIXME update expected to have all values
	}
	
	/**
	 * Can we get all the environmental data records that are available for the specified species?
	 */
	@Test
	public void testGetEnvironmentalDataJson01() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("Calotis hispidula"), Collections.emptyList(), 0, 20);
		List<EnvironmentDataRecord> response = result.getResponse();
		assertThat(response.size(), is(1));
		EnvironmentDataRecord record = response.get(0);
		assertThat(record.getDecimalLatitude(), is(-23.5318398476576d));
		assertThat(record.getDecimalLongitude(), is(138.321378247854d));
		assertThat(record.getGeodeticDatum(), is("GDA94"));
		assertThat(record.getEventDate(), is("1990-03-30"));
		assertThat(record.getMonth(), is(3));
		assertThat(record.getYear(), is(1990));
		assertThat(record.getLocationID(), is("aekos.org.au/collection/adelaide.edu.au/trend/SATFLB0025"));
		Collection<Entry> vars = record.getVariables();
		assertThat(vars.size(), is(7));
		// FIXME assert all values
	}
}
