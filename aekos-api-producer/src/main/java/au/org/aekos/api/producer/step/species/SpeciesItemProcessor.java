package au.org.aekos.api.producer.step.species;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

public class SpeciesItemProcessor implements ItemProcessor<InputSpeciesRecord, OutputSpeciesWrapper> {

    private Dataset dataset;
	private List<TraitExtractor> extractors;
	
	@Override
	public OutputSpeciesWrapper process(InputSpeciesRecord item) throws Exception {
		OutputSpeciesRecord speciesRecord = new OutputSpeciesRecord(item);
		List<TraitRecord> traitRecords = new LinkedList<>();
		Model model = dataset.getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		for (TraitExtractor curr : extractors) {
			TraitRecord trait = curr.doExtractOn(subject, item.getId());
			traitRecords.add(trait);
		}
		return new OutputSpeciesWrapper(speciesRecord, traitRecords);
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setExtractors(List<TraitExtractor> extractors) {
		this.extractors = extractors;
	}
}
