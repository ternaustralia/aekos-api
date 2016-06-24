package au.org.aekos.service.retrieval;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/applicationContext-test.xml")
public class JossDataRetrievalServiceTest {

	@Value("${joss.csv.resourcePath}")
	private String jossDataResourcePath;

	@Value("${joss.tenant-name}")
	private String jossTenantName;
	
	public String getJossDataResourcePath() {
		if(jossDataResourcePath == null){
			return "doggone-it.jpg";
		}
		return jossDataResourcePath;
	}

	
	/**
	 * Upload a bunch of bytes to the object store and get the return handle
	 */
	@Test
	public void testUploadBunchOfBytes() {
		
		String expectedURL = jossTenantName; // + more stuff
		JossDataProvisionService objectUnderTest = new JossDataProvisionService();
		
		
		try (InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(getJossDataResourcePath());
		    InputStreamReader isr = new InputStreamReader(in);) {

				
			URL result = objectUnderTest.storeData(in);
			assertThat(result, is(expectedURL));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
}

