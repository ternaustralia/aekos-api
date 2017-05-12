package au.org.aekos.api.producer.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What? No abstract methods? Just call doExtractorLoop and use the callback, it's cleaner.
 */
public abstract class AbstractItemProcessor<T extends AttributeExtractor> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractItemProcessor.class);
    private Dataset dataset;
	private List<T> extractors;
	private final Map<String, Counter> errorCounters = new HashMap<>();
	
	protected void doExtractorLoop(Consumer<T> callback) {
		for (T curr : extractors) {
			try {
				callback.accept(curr);
			} catch (MissingDataException e) {
				logErrorFor(curr);
				if (logger.isDebugEnabled()) {
					logger.debug("Trait extraction problem", e);
				}
			}
		}
	}
	
	protected Model getNamedModel(String name) {
		return dataset.getNamedModel(name);
	}
	
	public void reportProblems() {
		for (Entry<String, Counter> curr : errorCounters.entrySet()) {
			String extractorId = curr.getKey();
			Integer errorCount = curr.getValue().getValue();
			logger.warn(String.format("%s encountered %d errors when extracting '%s' traits", getClass().getSimpleName(), errorCount, extractorId));
		}
	}
	
	void logErrorFor(AttributeExtractor ex) {
		String id = ex.getId();
		Counter counter = errorCounters.get(id);
		if (counter == null) {
			counter = new Counter();
			errorCounters.put(id, counter);
		}
		counter.increment();
	}
	
	private static class Counter {
		private int i = 0;
		
		void increment() {
			i++;
		}
		
		int getValue() {
			return i;
		}
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setExtractors(List<T> extractors) {
		this.extractors = extractors;
	}
}
