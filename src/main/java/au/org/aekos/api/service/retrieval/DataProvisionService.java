package au.org.aekos.api.service.retrieval;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Stores the supplied file somewhere and returns the URL to access it
 * that can be passed to the client.
 */
public interface DataProvisionService {

	URL storeData(InputStream is) throws MalformedURLException;
}
