package au.org.aekos.api.service.vocab;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JenaVocabService implements VocabService {

	@Autowired
	@Qualifier("owlModel")
	private OntModel owlModel;
	
	@Value("${aekos-api.owl-file.namespace}")
	private String namespace;

	@Override
	public String getLabelForPropertyCode(String code) {
		DatatypeProperty datatypeProp = owlModel.getDatatypeProperty(namespace + code);
		if (datatypeProp == null) {
			return null;
		}
		return datatypeProp.getProperty(SKOS.prefLabel).getString();
	}

	public void setOwlModel(OntModel owlModel) {
		this.owlModel = owlModel;
	}

	public void setAekosOntologyNamespace(String namespace) {
		this.namespace = namespace;
	}
}
