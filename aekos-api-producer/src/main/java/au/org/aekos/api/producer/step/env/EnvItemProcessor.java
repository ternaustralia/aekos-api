package au.org.aekos.api.producer.step.env;

import java.util.LinkedList;
import java.util.List;

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
		// TODO get temporal information
		String eventDate = "FIXME";
		int month = 1;
		int year = 1;
		OutputEnvRecord envRecord = new OutputEnvRecord(item, eventDate, month, year);
		Model model = getNamedModel(item.getRdfGraph());
		Resource subject = model.getResource(item.getRdfSubject());
		List<AttributeRecord> variables = new LinkedList<>();
		List<Resource> observedItems = getObservedItems(subject);
		for (Resource currItem : observedItems) {
			doExtractorLoop(currExtractor -> {
				if (!currExtractor.canHandle(currItem)) {
					return;
				}
				AttributeRecord variable = currExtractor.doExtractOn(currItem, item.getLocationID());
				variables.add(variable);
			});
		}
		return new OutputEnvWrapper(envRecord, variables);
	}

	private List<Resource> getObservedItems(Resource subject) {
		List<Resource> result = new LinkedList<>();
		List<Resource> views = getViews(subject);
		for (Resource currView : views) {
			Bag observedItemsBag;
			try {
				observedItemsBag = helper.getBag(currView, "observeditems");
			} catch (Exception e) {
				// not all views need to have the property
				continue;
			}
			for (NodeIterator it = observedItemsBag.iterator(); it.hasNext();) {
				RDFNode next = it.next();
				result.add(next.asResource());
			}
		}
		return result;
	}

	private List<Resource> getViews(Resource subject) {
		List<Resource> result = new LinkedList<>();
		String propertyName = "views";
		Bag viewsBag = helper.getBag(subject, propertyName);
		for (NodeIterator it = viewsBag.iterator(); it.hasNext();) {
			RDFNode next = it.next();
			result.add(next.asResource());
		}
		return result;
	}
	
	public void setHelper(ExtractionHelper helper) {
		this.helper = helper;
	}
}
