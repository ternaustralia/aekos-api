package au.org.aekos.service.retrieval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Configuration
@PropertySource("classpath:application.properties")

@Service
public class JossDataProvisionService implements DataProvisionService {

	/**
	 * Read the account settings 
	 */
	@Value("${joss.username}")
	private String username;
	
	@Value("${joss.password}")
	private String password;
		
	@Value("${joss.auth-url}")
	private String authUrl;

	@Value("${joss.tenant-id}")
	private String tenantId;

	@Value("${joss.tenant-name}")
	private String tenantName;

	private AccountConfig config;
	private Account account;
	
	// This was breaking with the init of the factory complaining that it cannot set config (which is a final var).
	// So, maybe the docs are stale..
	// Optional to adjust this to work with autowired later on if we want
	// http://joss.javaswift.org/spring.html
	//@Autowired
	//private Account account;
	
	private final String AEKOS_BUCKET_NAME = "aekos-data-store";
	private final String AEKOS_BUCKET_ITEM_STUB = "aekos-data-item";
	
	/**
	 * Login to SWIFT when we are created 
	 */
	public JossDataProvisionService() {
		super();
		loginToAccount();
	}

	private void loginToAccount() {
		config = new AccountConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setAuthUrl(authUrl);
		config.setTenantId(tenantId);
		config.setTenantName(tenantName);
	    account = new AccountFactory(config).createAccount();
	}
	
	// Check to see if the container already exists and if not, create it
	private Container getContainer() {
		
	    Collection<Container> containers = account.list();
	    for (Container currentContainer : containers) {
	        if (AEKOS_BUCKET_NAME == currentContainer.getName()) {
	        	return (currentContainer);
	        }	    
	    }
        Container container = account.getContainer(AEKOS_BUCKET_NAME);
	    container.create();
	    container.makePublic();	

	    return container;
	}
	
	
	@Override
	public URL storeData(InputStream is) {
		Container container = getContainer();
	    
		String uuid = UUID.randomUUID().toString();
	    StoredObject object = container.getObject(AEKOS_BUCKET_ITEM_STUB + "_" + uuid);
	    object.uploadObject(is);
	    System.out.println("Public URL: "+object.getPublicURL());

		return null;
	}
	
    public static void main(String[] args) {
        // TODO - Run some tests
    	JossDataProvisionService service = new JossDataProvisionService();

    	final String targetName = "dog.png"; 
    	Container container = service.getContainer();
    	StoredObject object = container.getObject(targetName);
		try {
			InputStream targetStream = new FileInputStream(new File("/dog.png"));
	    	service.storeData(targetStream);

			System.out.println("Last modified:  "+object.getLastModified());
	        System.out.println("ETag:           "+object.getEtag());
	        System.out.println("Content type:   "+object.getContentType());
	        System.out.println("Content length: "+object.getContentLength());
		    System.out.println("Public URL: "    +object.getPublicURL());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

}
