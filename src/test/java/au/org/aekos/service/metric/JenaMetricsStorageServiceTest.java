package au.org.aekos.service.metric;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import au.org.aekos.model.AbstractParams;
import au.org.aekos.model.TraitDataParams;
import au.org.aekos.service.auth.AekosApiAuthKey;
import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.metric.MetricsStorageService.RequestType;

public class JenaMetricsStorageServiceTest {

	/**
	 * Can we record a request?
	 */
	@Test
	public void testRecordRequest01() throws InvalidKeyException {
		JenaMetricsStorageService objectUnderTest = new JenaMetricsStorageService();
		Model metricsModel = ModelFactory.createDefaultModel();
		objectUnderTest.setMetricsModel(metricsModel);
		AbstractParams params = new TraitDataParams(0, 20, Arrays.asList("atriplex vesicaria"), Arrays.asList("Height"));
		AekosApiAuthKey authKey = new AekosApiAuthKey("CAFEBABE1234");
		objectUnderTest.recordRequest(authKey, RequestType.TRAIT_DATA, params);
		Writer out = new StringWriter();
		metricsModel.write(out, "TURTLE");
		assertThat(out.toString(), is(
			"[ <http://www.aekos.org.au/api/1.0/metrics#authKey>\n" +
			"          \"CAFEBABE1234\" ;\n" +
			"  <http://www.aekos.org.au/api/1.0/metrics#requestType>\n" +
			"          \"TRAIT_DATA\"\n] .\n"));
		// TODO more assertions
	}
}
