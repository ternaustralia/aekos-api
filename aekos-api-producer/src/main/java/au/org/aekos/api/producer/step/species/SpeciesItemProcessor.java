package au.org.aekos.api.producer.step.species;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;

public class SpeciesItemProcessor implements ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> {

    private static final Logger log = LoggerFactory.getLogger(SpeciesItemProcessor.class);

	@Override
	public OutputSpeciesWrapper process(InputSpeciesRecord item) throws Exception {
		OutputSpeciesRecord speciesRecord = new OutputSpeciesRecord(item);
		// TODO enrich with trait information
//		List<TraitRecord> traitRecords = item.getTraits().stream()
//				.map(e -> new TraitRecord(speciesRecordId, e.getName(), e.getValue(), e.getUnits()))
//				.collect(Collectors.toList());
		return new OutputSpeciesWrapper(speciesRecord, Collections.emptyList() /*FIXME*/);
	}
}
