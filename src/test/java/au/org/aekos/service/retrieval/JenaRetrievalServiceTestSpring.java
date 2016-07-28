package au.org.aekos.service.retrieval;

import static au.org.aekos.TraitOrEnvVarMatcher.isTraitOrVar;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.EnvironmentDataResponse;
import au.org.aekos.model.ResponseHeader;
import au.org.aekos.model.SpeciesDataResponse;
import au.org.aekos.model.TraitDataRecord;
import au.org.aekos.model.TraitDataResponse;
import au.org.aekos.model.TraitOrEnvironmentalVariable;

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

	@Autowired
	@Qualifier("testGetTraitDataCsv01_expected")
	private String testGetTraitDataCsv01_expected;

	@Autowired
	@Qualifier("testGetAllSpeciesDataCsv01_expected")
	private String testGetAllSpeciesDataCsv01_expected;
	
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
		assertThat(result.getResponseHeader().getNumFound(), is(2));
	}
	
	/**
	 * Can we defend against a SPARQL injection attack?
	 */
	@Test
	public void testGetSpeciesDataJson04() throws Throwable {
		SpeciesDataResponse result = objectUnderTest.getSpeciesDataJson(Arrays.asList("\" UNDEF \""), 0, 1);
		assertThat("should be nothing because no species match the escaped text", result.getResponse().size(), is(0));
		assertThat(result.getResponseHeader().getNumFound(), is(0));
	}

	/**
	 * Can we get all the species records as JSON?
	 */
	@Test
	public void testGetAllSpeciesDataJson01() throws Throwable {
		SpeciesDataResponse result = objectUnderTest.getAllSpeciesDataJson(0, 10);
		assertThat(result.getResponse().size(), is(4));
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
	 * Can we get all the species records as CSV?
	 */
	@Test
	public void testGetAllSpeciesDataCsv01() throws Throwable {
		Writer writer = new StringWriter();
		objectUnderTest.getAllSpeciesDataCsv(0, 20, writer);
		String compareStr = testGetAllSpeciesDataCsv01_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetAllSpeciesDataCsv01_expected.replaceAll("\r", "");
		}
		assertEquals(writer.toString(), compareStr);
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
			compareStr = testGetEnvironmentalDataCsv01_expected.replaceAll("\r", "");
		}
		assertEquals(compareStr, writer.toString());
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
		assertThat(record.getBibliographicCitation(), is("TERN Australian Transect Network, Biomes of Australian Soils Consortium (2015). Transects for Environmental "
				+ "Monitoring and Decision Making (TREND) (2013-2015) and TREND-Biome of Australia Soil Environments (BASE) - "
				+ "Soil samples for physical structure and chemical analysis (14 sites) throughout Australia (2013), Version 11/2015. "
				+ "Persistent Hyperlink: http://www.aekos.org.au/collection/adelaide.edu.au/trend. &#198;KOS Data Portal, rights "
				+ "owned by The University of Adelaide (TERN Australian Transects Network - TREND), Bioplatforms Australia Ltd. "
				+ "Accessed [dd mmm yyyy, e.g. 01 Jan 2016]."));
		assertThat(record.getSamplingProtocol(), is("aekos.org.au/collection/adelaide.edu.au/trend"));
		Collection<TraitOrEnvironmentalVariable> vars = record.getVariables();
		assertThat(vars.size(), is(7));
		Iterator<TraitOrEnvironmentalVariable> varsIterator = vars.iterator();
		assertThat(varsIterator.next(), isTraitOrVar("rainfallTotal", "0.4", "millilitres"));
		assertThat(varsIterator.next(), isTraitOrVar("rainfallTotal", "49.8", "millilitres"));
		assertThat(varsIterator.next(), isTraitOrVar("rainfallMean", "0.013333333", "millilitres"));
		assertThat(varsIterator.next(), isTraitOrVar("rainfallMean", "1.606451613", "millilitres"));
		assertThat(varsIterator.next(), isTraitOrVar("temperatureMean", "28.12631579", "degrees Celcius"));
		assertThat(varsIterator.next(), isTraitOrVar("temperatureMaximum", "38.03157895", "degrees Celcius"));
		assertThat(varsIterator.next(), isTraitOrVar("temperatureMinimum", "16.86315789", "degrees Celcius"));
	}
	
	/**
	 * Can we get the total numFound?
	 */
	@Test
	public void testGetEnvironmentalDataJson02() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("Calotis hispidula", "Rosa canina"), Collections.emptyList(), 0, 1);
		assertThat(result.getResponse().size(), is(1));
		assertThat(result.getResponseHeader().getNumFound(), is(2));
	}
	
	/**
	 * Can we get all the environmental data variables that aren't already tested?
	 */
	@Test
	public void testGetEnvironmentalDataJson03() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("Rosa canina"), Collections.emptyList(), 0, 20);
		List<EnvironmentDataRecord> response = result.getResponse();
		assertThat(response.size(), is(1));
		EnvironmentDataRecord record = response.get(0);
		assertThat(record.getLocationID(), is("aekos.org.au/collection/adelaide.edu.au/trend/SATFLB0026"));
		Collection<TraitOrEnvironmentalVariable> vars = record.getVariables();
		assertThat(vars.size(), is(20));
		Iterator<TraitOrEnvironmentalVariable> varsIterator = vars.iterator();
		assertThat(varsIterator.next(), isTraitOrVar("disturbanceEvidenceCover", "50", "percent"));
		assertThat(varsIterator.next(), isTraitOrVar("slope", "10", "degrees"));
		assertThat(varsIterator.next(), isTraitOrVar("aspect", "230", "degrees"));
		assertThat(varsIterator.next(), isTraitOrVar("erosionEvidenceType", "No evidence", ""));
		assertThat(varsIterator.next(), isTraitOrVar("surfaceType", "Flat", ""));
		assertThat(varsIterator.next(), isTraitOrVar("erosionEvidenceState", "Good", ""));
		assertThat(varsIterator.next(), isTraitOrVar("visibleFireEvidence", "No evidence", ""));
		assertThat(varsIterator.next(), isTraitOrVar("soilTexture", "Coarse", ""));
		assertThat(varsIterator.next(), isTraitOrVar("soilType", "Clay", ""));
		assertThat(varsIterator.next(), isTraitOrVar("disturbanceEvidenceType", "No effective disturbance", ""));
		assertThat(varsIterator.next(), isTraitOrVar("latestLandUse", "Farming", ""));
		assertThat(varsIterator.next(), isTraitOrVar("ph", "5.4", "pH"));
		assertThat(varsIterator.next(), isTraitOrVar("silt", "A lot", "siltiness"));
		assertThat(varsIterator.next(), isTraitOrVar("clay", "Very", "dunno"));
		assertThat(varsIterator.next(), isTraitOrVar("sand", "it's everywhere", "sandiness"));
		assertThat(varsIterator.next(), isTraitOrVar("totalOrganicCarbon", "1.34", "percent"));
		assertThat(varsIterator.next(), isTraitOrVar("electricalConductivity", "4", "millisiemens per metre"));
		assertThat(varsIterator.next(), isTraitOrVar("windMeanAverageDirection", "270", "degrees"));
		assertThat(varsIterator.next(), isTraitOrVar("windMeanAverage", "13", "km/h"));
		assertThat(varsIterator.next(), isTraitOrVar("windMaximumMean", "30", "km/h"));
	}
	
	/**
	 * Can we filter the variables that are returned?
	 */
	@Test
	public void testGetEnvironmentalDataJson04() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("Calotis hispidula", "Rosa canina"),
				Arrays.asList("aspect", "soilType", "sand"), 0, 20);
		assertThat(result.getResponseHeader().getNumFound(), is(1));
		List<EnvironmentDataRecord> response = result.getResponse();
		assertThat(response.size(), is(1));
		EnvironmentDataRecord record = response.get(0);
		assertThat(record.getLocationID(), is("aekos.org.au/collection/adelaide.edu.au/trend/SATFLB0026"));
		Collection<TraitOrEnvironmentalVariable> vars = record.getVariables();
		assertThat(vars.size(), is(3));
		Iterator<TraitOrEnvironmentalVariable> varsIterator = vars.iterator();
		assertThat(varsIterator.next(), isTraitOrVar("aspect", "230", "degrees"));
		assertThat(varsIterator.next(), isTraitOrVar("soilType", "Clay", ""));
		assertThat(varsIterator.next(), isTraitOrVar("sand", "it's everywhere", "sandiness"));
	}
	
	/**
	 * Can we defend against a SPARQL injection attack on the species name?
	 */
	@Test
	public void testGetEnvironmentalDataJson05() throws Throwable {
		EnvironmentDataResponse result = objectUnderTest.getEnvironmentalDataJson(Arrays.asList("\" UNDEF \""), Collections.emptyList(), 0, 1);
		assertThat("should be nothing because no species match the escaped text", result.getResponse().size(), is(0));
		assertThat(result.getResponseHeader().getNumFound(), is(0));
	}
	
	/**
	 * Can we map all the variables for a trait data record?
	 */
	@Test
	public void testGetTraitDataJson01() throws Throwable {
		TraitDataResponse result = objectUnderTest.getTraitDataJson(Arrays.asList("Goodenia havilandii"), Collections.emptyList(), 0, 20);
		ResponseHeader header = result.getResponseHeader();
		assertThat(header.getPageNumber(), is(1));
		assertThat(header.getTotalPages(), is(1));
		assertThat(header.getNumFound(), is(1));
		List<TraitDataRecord> response = result.getResponse();
		assertThat(response.size(), is(1));
		TraitDataRecord record = response.get(0);
		Collection<TraitOrEnvironmentalVariable> traits = record.getTraits();
		assertThat(traits.size(), is(3));
		Iterator<TraitOrEnvironmentalVariable> traitsIterator = traits.iterator();
		assertThat(traitsIterator.next(), isTraitOrVar("phenology", "1", "thingies"));
		assertThat(traitsIterator.next(), isTraitOrVar("totalLength", "0.3", "meters"));
		assertThat(traitsIterator.next(), isTraitOrVar("heightOfBreak", "5", "meters"));
	}
	
	/**
	 * Can we correctly filter for trait names?
	 */
	@Test
	public void testGetTraitDataJson02() throws Throwable {
		TraitDataResponse result1 = objectUnderTest.getTraitDataJson(Arrays.asList("Calotis hispidula"), Collections.emptyList(), 0, 20);
		assertThat(result1.getResponseHeader().getNumFound(), is(2));
		TraitDataResponse result2 = objectUnderTest.getTraitDataJson(Arrays.asList("Calotis hispidula"), Arrays.asList("lifeStage"), 0, 20);
		assertThat(result2.getResponseHeader().getNumFound(), is(1));
		List<TraitDataRecord> response = result2.getResponse();
		assertThat(response.size(), is(1));
		TraitDataRecord record = response.get(0);
		assertThat(record.getYear(), is(2013));
	}
	
	/**
	 * Can we defend against a SPARQL injection attack on the species name?
	 */
	@Test
	public void testGetTraitDataJson03() throws Throwable {
		TraitDataResponse result = objectUnderTest.getTraitDataJson(Arrays.asList("\" UNDEF \""), Collections.emptyList(), 0, 20);
		assertThat("should be nothing because no species match the escaped text", result.getResponse().size(), is(0));
		assertThat(result.getResponseHeader().getNumFound(), is(0));
	}
	
	/**
	 * Can we get the trait data CSV?
	 */
	@Test
	public void testGetTraitDataCsv01() throws Throwable {
		Writer writer = new StringWriter();
		objectUnderTest.getTraitDataCsv(Arrays.asList("Calotis hispidula"), Collections.emptyList(), 0, 20, writer);
		String compareStr = testGetTraitDataCsv01_expected;
		if(SystemUtils.IS_OS_WINDOWS){
			compareStr = testGetTraitDataCsv01_expected.replaceAll("\r", "");
		}
		assertEquals(compareStr, writer.toString());
	}
	
	/**
	 * Can we count species records when there are some?
	 */
	@Test
	public void testGetTotalRecordsHeldForSpeciesName01() {
		int result = objectUnderTest.getTotalRecordsHeldForSpeciesName("Calotis hispidula");
		assertThat(result, is(2));
	}
	
	/**
	 * Can we count species records when there are none?
	 */
	@Test
	public void testGetTotalRecordsHeldForSpeciesName02() {
		int result = objectUnderTest.getTotalRecordsHeldForSpeciesName("blah blah");
		assertThat(result, is(0));
	}
}
