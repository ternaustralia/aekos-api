package au.org.aekos.api.producer.step.species;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.step.MissingDataException;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class SpeciesItemProcessor implements ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(SpeciesItemProcessor.class);
    private Dataset dataset;
	private List<TraitExtractor> extractors;
	private final Map<String, Counter> errorCounters = new HashMap<>();
	
	@Override
	public OutputSpeciesWrapper process(InputSpeciesRecord item) throws Exception {
		OutputSpeciesRecord speciesRecord = new OutputSpeciesRecord(item);
		List<TraitRecord> traitRecords = new LinkedList<>();
		Model model = dataset.getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		for (TraitExtractor curr : extractors) {
			try {
				TraitRecord trait = curr.doExtractOn(subject, item.getId());
				traitRecords.add(trait);
			} catch (MissingDataException e) {
				logErrorFor(curr);
				if (logger.isDebugEnabled()) {
					logger.debug("Trait extraction problem", e);
				}
			}
		}
		return new OutputSpeciesWrapper(speciesRecord, traitRecords);
	}
	
	public void reportProblems() {
		for (Entry<String, Counter> curr : errorCounters.entrySet()) {
			String extractorId = curr.getKey();
			Integer errorCount = curr.getValue().getValue();
			logger.warn(String.format("%s encountered %d errors when extracting '%s' traits", getClass().getSimpleName(), errorCount, extractorId));
		}
	}
	
	void logErrorFor(TraitExtractor ex) {
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

	public void setExtractors(List<TraitExtractor> extractors) {
		this.extractors = extractors;
	}
}
