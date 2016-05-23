package au.org.aekos.service.retrieval;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/org/aekos/applicationContext-test.xml")
public class JossDataProvisionServiceTest {

	@Autowired
	private JossDataProvisionService objectUnderTest;
	
	@Test
	@Ignore // needs to be modified before it can run everytime
	public void testStoreData01() throws Throwable {
    	final String targetName = "dog.png"; 
    	Container container = objectUnderTest.getContainer();
    	StoredObject object = container.getObject(targetName);
		InputStream targetStream = new FileInputStream(new File("/dog.png"));
    	objectUnderTest.storeData(targetStream);

		System.out.println("Last modified:  "+object.getLastModified());
        System.out.println("ETag:           "+object.getEtag());
        System.out.println("Content type:   "+object.getContentType());
        System.out.println("Content length: "+object.getContentLength());
	    System.out.println("Public URL: "    +object.getPublicURL());
	}
}
