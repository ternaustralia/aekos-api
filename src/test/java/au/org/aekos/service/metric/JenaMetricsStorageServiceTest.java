package au.org.aekos.service.metric;

import static au.org.aekos.TestUtils.loadMetric;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
		JenaMetricsStorageService objectUnderTest = new JenaMetricsStorageService();
		ReflectionTestUtils.setField(objectUnderTest, "eventDateProvider", new JenaMetricsStorageService.EventDateProvider() {
			@Override
			public long getEventDate() {
				return 1468917533333l;
			}
		});
		Model metricsModel = ModelFactory.createDefaultModel();
		objectUnderTest.setMetricsModel(metricsModel);
		objectUnderTest.setIdProvider(new IdProvider() {
			@Override
			public String nextId() {
				return "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963";
			}
		});
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
		JenaMetricsStorageService objectUnderTest = new JenaMetricsStorageService();
		ReflectionTestUtils.setField(objectUnderTest, "eventDateProvider", new JenaMetricsStorageService.EventDateProvider() {
			@Override
			public long getEventDate() {
				return 1468917533333l;
			}
		});
		Model metricsModel = ModelFactory.createDefaultModel();
		objectUnderTest.setMetricsModel(metricsModel);
		objectUnderTest.setIdProvider(new IdProvider() {
			@Override
			public String nextId() {
				return "urn:cbfbaccb-43c6-47a9-bddf-93a4c0077963";
			}
		});
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.V1_TRAIT_VOCAB);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertEquals(loadMetric("testRecordResponse02_expected.ttl"), out.toString());
	}
}
