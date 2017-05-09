package au.org.aekos.api.producer.step.species;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.stereotype.Component;

import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.SpeciesRecord;
import au.org.aekos.api.producer.step.species.out.TraitRecord;

@Component
public class AekosRelationalCsvWriter extends AbstractItemStreamItemWriter<OutputSpeciesWrapper> {

	private AbstractItemStreamItemWriter<SpeciesRecord> speciesWriter;
	private AbstractItemStreamItemWriter<TraitRecord> traitWriter;

	@Override
	public void write(List<? extends OutputSpeciesWrapper> items) throws Exception {
		for (OutputSpeciesWrapper curr : items) {
			speciesWriter.write(curr.getSpeciesRecord());
			traitWriter.write(curr.getTraitRecords());
		}
	}
	
	@Override
	public void open(ExecutionContext executionContext) {
		super.open(executionContext);
		speciesWriter.open(executionContext);
		traitWriter.open(executionContext);
	}

	@Override
	public void close() {
		super.close();
		speciesWriter.close();
		traitWriter.close();
	}

	public void setSpeciesWriter(AbstractItemStreamItemWriter<SpeciesRecord> speciesWriter) {
		this.speciesWriter = speciesWriter;
	}

	public void setTraitWriter(AbstractItemStreamItemWriter<TraitRecord> traitWriter) {
		this.traitWriter = traitWriter;
	}
}
