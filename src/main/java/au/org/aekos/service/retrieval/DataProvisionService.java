package au.org.aekos.service.retrieval;

import java.io.InputStream;
import java.net.URL;

/**
 * Stores the supplied file somewhere and returns the URL to access it
 * that can be passed to the client.
 */
public interface DataProvisionService {

	URL storeData(InputStream is);
}
