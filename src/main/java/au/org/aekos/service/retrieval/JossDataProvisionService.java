package au.org.aekos.service.retrieval;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JossDataProvisionService implements DataProvisionService {

	private static final Logger logger = LoggerFactory.getLogger(JossDataProvisionService.class);

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
	
	@Value("${joss.disable-eager-login}")
	private boolean disableEagerLogin;

	private AccountConfig config;
	private Account account;
	
	// This was breaking with the init of the factory complaining that it cannot set config (which is a final var).
	// So, maybe the docs are stale..
	// Optional to adjust this to work with autowired later on if we want
	// http://joss.javaswift.org/spring.html
	//@Autowired
	//private Account account;
	
	public static final String AEKOS_BUCKET_NAME = "aekos-api-downloads-data-store";
	public static final String AEKOS_BUCKET_ITEM_STUB = "aekos-api-downloads-";
	
	@PostConstruct
	public void loginToAccount() {
		if (disableEagerLogin) {
			return;
		}
		config = new AccountConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setAuthUrl(authUrl);
		config.setTenantId(tenantId);
		config.setTenantName(tenantName);
	    account = new AccountFactory(config).createAccount();
	}
	
	// Check to see if the container already exists and if not, create it
	public Container getContainer() {
		
		if (account == null) {
			loginToAccount();
		}
		
	    Collection<Container> containers = account.list();
	    for (Container currentContainer : containers) {
	        if (AEKOS_BUCKET_NAME == currentContainer.getName()) {
	        	return (currentContainer);
	        }	    
	    }
        Container container = account.getContainer(AEKOS_BUCKET_NAME);
        if (!container.exists()) {
	        try {
			    container.create();
			    container.makePublic();	
	        } catch (Exception e) {
				e.printStackTrace();
			}
        }

	    return container;
	}
	
	
	@Override
	public URL storeData(InputStream is) throws MalformedURLException {
		Container container = getContainer();
	    
		String uuid = UUID.randomUUID().toString();
	    StoredObject object = container.getObject(AEKOS_BUCKET_ITEM_STUB + uuid);
	    object.uploadObject(is);
	    URL url = new URL(object.getPublicURL());
	    
	    logger.debug("Public URL: " + url);
	    return (url);
	}
}
