package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/applicationContext-test.xml")
public class JossDataProvisionServiceTest {

	@Value("${joss.csv.resourcePath}")
	private String jossDataResourcePath;
	
	@Value("${joss.tenant-id}")
	private String jossTenantId;

	@Autowired
	private JossDataProvisionService objectUnderTest;
	
	@Test
	@Ignore // needs to be modified before it can run everytime
	public void testStoreData01() throws Throwable {
    	InputStream in = this.getClass().getClassLoader().getResourceAsStream(jossDataResourcePath);
    	URL handle = objectUnderTest.storeData(in);

		String compareStr = "https://swift.rc.nectar.org.au:8888/v1/AUTH_" + jossTenantId + "/" + objectUnderTest.AEKOS_BUCKET_NAME + "/" + objectUnderTest.AEKOS_BUCKET_ITEM_STUB;
    	assertThat(handle.toString(), startsWith(compareStr));
	}
}
