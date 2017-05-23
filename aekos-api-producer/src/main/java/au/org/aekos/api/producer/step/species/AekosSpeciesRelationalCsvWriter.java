package au.org.aekos.api.producer.step.species;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.stereotype.Component;

import au.org.aekos.api.producer.step.species.out.OutputSpeciesRecord;
import au.org.aekos.api.producer.step.species.out.OutputSpeciesWrapper;
import au.org.aekos.api.producer.step.species.out.SpeciesTraitRecord;

@Component
public class AekosSpeciesRelationalCsvWriter extends AbstractItemStreamItemWriter<OutputSpeciesWrapper> {

	private AbstractItemStreamItemWriter<OutputSpeciesRecord> speciesWriter;
	private AbstractItemStreamItemWriter<SpeciesTraitRecord> traitWriter;

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

	public void setSpeciesWriter(AbstractItemStreamItemWriter<OutputSpeciesRecord> speciesWriter) {
		this.speciesWriter = speciesWriter;
	}

	public void setTraitWriter(AbstractItemStreamItemWriter<SpeciesTraitRecord> traitWriter) {
		this.traitWriter = traitWriter;
	}
}
