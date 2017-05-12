package au.org.aekos.api.producer.step.species;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.step.AbstractItemProcessor;
import au.org.aekos.api.producer.step.AttributeExtractor;
import au.org.aekos.api.producer.step.AttributeRecord;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;

public class SpeciesItemProcessor extends AbstractItemProcessor<AttributeExtractor> implements ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> {

	@Override
	public OutputSpeciesWrapper process(InputSpeciesRecord item) throws Exception {
		OutputSpeciesRecord speciesRecord = new OutputSpeciesRecord(item);
		List<AttributeRecord> traitRecords = new LinkedList<>();
		Model model = getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		doExtractorLoop(curr -> {
			AttributeRecord trait = curr.doExtractOn(subject, item.getId());
			traitRecords.add(trait);
		});
		return new OutputSpeciesWrapper(speciesRecord, traitRecords);
	}
}
