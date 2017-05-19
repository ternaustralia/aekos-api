package au.org.aekos.api.producer.step.env;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.springframework.batch.item.ItemProcessor;

import au.org.aekos.api.producer.ExtractionHelper;
import au.org.aekos.api.producer.step.AbstractItemProcessor;
import au.org.aekos.api.producer.step.AttributeRecord;
import au.org.aekos.api.producer.step.BagAttributeExtractor;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvRecord;
import au.org.aekos.api.producer.step.env.out.OutputEnvWrapper;

public class EnvItemProcessor extends AbstractItemProcessor<BagAttributeExtractor> implements ItemProcessor<InputEnvRecord, OutputEnvWrapper> {

	private ExtractionHelper helper;
	
	@Override
	public OutputEnvWrapper process(InputEnvRecord item) throws Exception {
		OutputEnvRecord envRecord = new OutputEnvRecord(item);
		Model model = getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		List<AttributeRecord> variables = new LinkedList<>();
		processObservedItems(subject, observedItem -> {
			doExtractorLoop(currExtractor -> {
				if (!currExtractor.canHandle(observedItem)) {
					return;
				}
				AttributeRecord variable = currExtractor.doExtractOn(observedItem, item.getLocationID());
				variables.add(variable);
			});
		});
		return new OutputEnvWrapper(envRecord, variables);
	}

	private void processObservedItems(Resource subject, Consumer<Resource> elementCallback) {
		Bag observedItemsBag;
		try {
			observedItemsBag = helper.getBag(subject, "observeditems");
		} catch (Exception e) {
			// not all views need to have the property
			return;
		}
		for (NodeIterator it = observedItemsBag.iterator(); it.hasNext();) {
			RDFNode next = it.next();
			elementCallback.accept(next.asResource());
		}
	}
	
	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}
}
