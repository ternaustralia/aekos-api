package au.org.aekos.api.producer.step;

import org.apache.jena.rdf.model.Resource;

public interface BagAttributeExtractor extends AttributeExtractor {

	boolean canHandle(Resource subject);
}
