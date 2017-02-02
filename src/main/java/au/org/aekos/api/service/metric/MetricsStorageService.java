package au.org.aekos.api.service.metric;

import java.io.Writer;
import java.util.Map;

public interface MetricsStorageService extends RequestRecorder {

	/**
	 * Get a summary of how many times each request type has been called.
	 * 
	 * @return	mapping of request type to call count
	 */
	Map<RequestType, Integer> getRequestSummary();

	/**
	 * Writes the whole model as TURTLE RDF to the supplied writer
	 * 
	 * @param responseWriter	writer to write to
	 */
	void writeRdfDump(Writer writer);
}
