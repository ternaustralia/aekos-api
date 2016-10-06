package au.org.aekos.service.metric;

import static au.org.aekos.TestUtils.loadMetric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.metric.RequestRecorder.RequestType;

public class JenaMetricsStorageServiceTest {

	/**
	 * Can we record a request with species?
	 */
	@Test
	public void testRecordRequestWithSpecies01() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequestWithSpecies(authKey, RequestType.V1_TRAIT_BY_SPECIES, new String[] {"atriplex vesicaria"}, 1, 30);
		String modelTTL = getModelTurtleString(metricsDataset);
		assertEquals(loadMetric("testRecordRequestWithSpecies01_expected.ttl"), modelTTL);
	}
	
	/**
	 * Can we record a request with traits/env vars?
	 */
	@Test
	public void testRecordRequestWithTraitsOrEnvVars01() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequestWithTraitsOrEnvVars(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"trait1", "trait2"}, 1, 30);
		String modelTTL = getModelTurtleString(metricsDataset);
		assertEquals(loadMetric("testRecordRequestWithTraitsOrEnvVars01_expected.ttl"), modelTTL);
	}
	
	/**
	 * Can we record in a TDB instance using transactions?
	 */
	@Test
	public void testRecordRequest01() throws Throwable {
		Path tempDir = Files.createTempDirectory("testRecordRequest05");
		tempDir.toFile().deleteOnExit();
		Dataset metricsDataset = TDBFactory.createDataset(tempDir.toString());
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		Map<RequestType, Integer> result = objectUnderTest.getRequestSummary();
		assertThat(result.size(), is(1));
		assertThat(result.get(RequestType.V1_TRAIT_VOCAB), is(1));
	}

	/**
	 * Can we record a request with no parameters?
	 */
	@Test
	public void testRecordRequest02() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(1468917533333l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		String modelTTL = getModelTurtleString(metricsDataset);
		assertEquals(loadMetric("testRecordResponse02_expected.ttl"), modelTTL);
	}
	
	/**
	 * Can we record a request that takes species name(s)?
	 */
	@Test
	public void testRecordRequest03() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(1468955533888l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077aaa");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_SUMMARY, new String[] {"Acacia chrysella", "Acacia chrysocephala"});
		String modelTTL = getModelTurtleString(metricsDataset);
		assertEquals(loadMetric("testRecordResponse03_expected.ttl"), modelTTL);
	}
	
	/**
	 * Can we record a request that takes trait name(s) and paging information?
	 */
	@Test
	public void testRecordRequest04() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(1468955533888l, metricsDataset, "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077aaa");
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"Acacia chrysella", "Acacia chrysocephala"},
				new String[] {"averageHeight", "lifeForm"}, 100, 20);
		String modelTTL = getModelTurtleString(metricsDataset);
		assertEquals(loadMetric("testRecordResponse04_expected.ttl"), modelTTL);
	}
	
	/**
	 * Can we get a summary of the requests?
	 */
	@Test
	public void testGetRequestSummary01() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(
				new long[] {111l, 222l, 333l, 444l},
				metricsDataset,
				new String[] {"urn:aaa", "urn:bbb", "urn:ccc", "urn:ddd"});
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		objectUnderTest.recordRequestWithTraitsOrEnvVars(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 1, 20);
		objectUnderTest.recordRequestWithTraitsOrEnvVars(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 2, 20);
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_DATA_JSON, new String[] {"Acacia chrysocephala"}, new String[] {"trait1"}, 0, 20);
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
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(111l, metricsDataset, "urn:aaa");
		Map<RequestType, Integer> result = objectUnderTest.getRequestSummary();
		assertThat(result.size(), is(0));
	}

	/**
	 * Can we write an RDF dump when there are records to dump?
	 */
	@Test
	public void testWriteRdfDump01() throws InvalidKeyException {
		Dataset metricsDataset = DatasetFactory.create();
		JenaMetricsStorageService objectUnderTest = jmss(
				new long[] {111l, 222l, 333l, 444l},
				metricsDataset,
				new String[] {"urn:aaa", "urn:bbb", "urn:ccc", "urn:ddd"});
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		objectUnderTest.recordRequestWithTraitsOrEnvVars(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 1, 20);
		objectUnderTest.recordRequestWithTraitsOrEnvVars(authKey, RequestType.V1_SPECIES_BY_TRAIT, new String[] {"averageHeight", "lifeForm"}, 2, 20);
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_DATA_JSON, new String[] {"Acacia chrysocephala"}, new String[] {"trait1"}, 0, 20);
		StringWriter writer = new StringWriter();
		objectUnderTest.writeRdfDump(writer);
		assertEquals(loadMetric("testWriteRdfDump01_expected.ttl"), writer.toString());
	}
	
	private JenaMetricsStorageService jmss(long eventDate, Dataset metricsDataset, String idProviderNextValue) {
		return jmss(new long[] {eventDate}, metricsDataset, new String[] {idProviderNextValue});
	}
	
	private JenaMetricsStorageService jmss(long[] eventDates, Dataset metricsDataset, String[] idProviderNextValues) {
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
		result.setMetricsDataset(metricsDataset);
		result.setMetricsModel(metricsDataset.getDefaultModel());
		return result;
	}
	
	private String getModelTurtleString(Dataset metricsDataset) {
		Writer out = new StringWriter();
		metricsDataset.getDefaultModel().write(out, "TURTLE");
		return out.toString();
	}
}
