package au.org.aekos.api.producer.step.species;

import org.apache.jena.rdf.model.Resource;

import au.org.aekos.api.producer.step.species.out.TraitRecord;

public interface TraitExtractor {

	TraitRecord doExtractOn(Resource subject, String parentId);
}
