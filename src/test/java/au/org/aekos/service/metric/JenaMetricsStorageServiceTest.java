package au.org.aekos.service.metric;

import static au.org.aekos.TestUtils.loadMetric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.TraitDataParams;
import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;

public class JenaMetricsStorageServiceTest {

	/**
	 * Can we record a request with retrieval parameters?
	 */
	@Test
	public void testRecordRequest01() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsModel, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AbstractParams params = new TraitDataParams(0, 20, Arrays.asList("atriplex vesicaria"), Arrays.asList("Height"));
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_DATA_CSV, params);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("testRecordResponse01_expected.ttl"), out.toString());
	}
	
	/**
	 * Can we record a request with no parameters?
	 */
	@Test
	public void testRecordRequest02() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsModel, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("testRecordResponse02_expected.ttl"), out.toString());
	}
	
	/**
	 * Can we record a request that takes species name(s)?
	 */
	@Test
	public void testRecordRequest03() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(1468955533888l, metricsModel, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077aaa");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_SUMMARY, new String[] {"Acacia chrysella", "Acacia chrysocephala"});
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("testRecordResponse03_expected.ttl"), out.toString());
	}
	
	/**
	 * Can we record a request that takes trait name(s) and paging information?
	 */
	@Test
	public void testRecordRequest04() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(1468955533888l, metricsModel, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077aaa");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 100, 20);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("testRecordResponse04_expected.ttl"), out.toString());
	}
	
	/**
	 * Can we record in a TDB instance using transactions?
	 */
	@Test
	public void testRecordRequest05() throws Throwable {
		Path tempDir = Files.createTempDirectory("testRecordRequest05");
		tempDir.toFile().deleteOnExit();
		Model metricsModel = TDBFactory.createDataset(tempDir.toString()).getDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsModel, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		Map<RequestType, Integer> result = objectUnderTest.getRequestSummary();
		assertThat(result.size(), is(1));
		assertThat(result.get(RequestType.V1_TRAIT_VOCAB), is(1));
	}
	
	/**
	 * Can we get a summary of the requests?
	 */
	@Test
	public void testGetRequestSummary01() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(
				new long[] {111l, 222l, 333l, 444l},
				metricsModel,
				new String[] {"urn:aaa", "urn:bbb", "urn:ccc", "urn:ddd"});
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 0, 20);
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 20, 20);
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_DATA_JSON, new TraitDataParams(0, 20, Arrays.asList("Acacia chrysocephala"), Collections.emptyList()));
		Map<RequestType, Integer> result = objectUnderTest.getRequestSummary();
		assertThat(result.size(), is(3));
		assertThat(result.get(RequestType.V1_TRAIT_VOCAB), is(1));
		assertThat(result.get(RequestType.V1_SPECIES_BY_TRAIT), is(2));
		assertThat(result.get(RequestType.V1_TRAIT_DATA_JSON), is(1));
	}
	
	/**
	 * Can we survive getting a summary of the requests when there aren't any?
	 */
	@Test
	public void testGetRequestSummary02() throws InvalidKeyException {
		Model metricsModel = ModelFactory.createDefaultModel();
		JenaMetricsStorageService objectUnderTest = jmss(111l, metricsModel, "urn:aaa");
		Map<RequestType, Integer> result = objectUnderTest.getRequestSummary();
		assertThat(result.size(), is(0));
	}

	private JenaMetricsStorageService jmss(long eventDate, Model metricsModel, String idProviderNextValue) {
		return jmss(new long[] {eventDate}, metricsModel, new String[] {idProviderNextValue});
	}
	
	private JenaMetricsStorageService jmss(long[] eventDates, Model metricsModel, String[] idProviderNextValues) {
		JenaMetricsStorageService result = new JenaMetricsStorageService();
		ReflectionTestUtils.setField(result, "eventDateProvider", new JenaMetricsStorageService.EventDateProvider() {
			private int i = 0;
			private long[] values = eventDates;
			@Override
			public long getEventDate() {
				return values[i++];
			}
		});
		result.setIdProvider(new IdProvider() {
			private int i = 0;
			private String[] values = idProviderNextValues;
			@Override
			public String nextId() {
				return values[i++];
			}
		});
		result.setMetricsModel(metricsModel);
		return result;
	}
}
