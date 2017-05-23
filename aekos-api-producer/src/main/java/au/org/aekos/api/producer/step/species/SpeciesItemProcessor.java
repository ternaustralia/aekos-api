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
import au.org.aekos.api.producer.step.species.out.SpeciesTraitRecord;

public class SpeciesItemProcessor extends AbstractItemProcessor<AttributeExtractor> implements ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> {

	@Override
	public OutputSpeciesWrapper process(InputSpeciesRecord item) throws Exception {
		OutputSpeciesRecord speciesRecord = new OutputSpeciesRecord(item);
		List<SpeciesTraitRecord> traitRecords = new LinkedList<>();
		Model model = getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		doExtractorLoop(curr -> {
			AttributeRecord trait = curr.doExtractOn(subject);
			traitRecords.add(new SpeciesTraitRecord(item.getId(), trait.getName(), trait.getValue(), trait.getUnits()));
		});
		return new OutputSpeciesWrapper(speciesRecord, traitRecords);
	}
}
