package au.org.aekos.api.loader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

public class Thingy {

	private static final String WAD = "http://www.aekos.org.au/ontology/1.0.0/wadec_ravensthorpe#";
	private static final String AEKOS = "http://www.aekos.org.au/ontology/1.0.0#";
	private final Set<Resource> seen = new HashSet<>();
	private final Set<String> propLocalNameBlacklist = new HashSet<>();
	
	public static void main(String[] args) throws Throwable {
		System.out.println("Running");
		new Thingy().run();
		System.out.println("Done");
	}

	private void run() throws Throwable {
		initBlacklist();
		Model source = ModelFactory.createDefaultModel();
		source.read(new FileInputStream("/data/all.ttl"), null, "TURTLE");
		Model output = ModelFactory.createDefaultModel();
		output.setNsPrefix("", AEKOS);
		output.setNsPrefix("wad", WAD);
		Resource slsg = source.getResource(WAD + "STUDYLOCATIONSUBGRAPH-T1485141126269");
		processResource(slsg, output);
		output.write(new FileOutputStream("/data/subgraph.ttl"), "TURTLE");
	}

	private void processResource(Resource res, Model output) {
		if (!seen.add(res)) {
			return;
		}
		int bagCounter = 0;
		for (StmtIterator it = res.listProperties(); it.hasNext();) {
			Statement curr = it.next();
			String currPredLocalName = curr.getPredicate().getLocalName();
			if (propLocalNameBlacklist.contains(currPredLocalName)) {
				continue;
			}
			if (res.hasProperty(RDF.type, RDF.Bag)
//					getProperty(RDF.type).getObject().asResource().getURI()
					) {
				if (++bagCounter > 5) {
					continue;
				}
			}
			output.add(curr);
			RDFNode currObject = curr.getObject();
			if (currObject.isResource()) {
				processResource(currObject.asResource(), output);
			}
		}
	}
	
	private void initBlacklist() {
		propLocalNameBlacklist.add("abstract");
		propLocalNameBlacklist.add("conditions");
		propLocalNameBlacklist.add("entitydisplay");
		propLocalNameBlacklist.add("extractioncontainer");
		propLocalNameBlacklist.add("extractioncontainerfaunaindiv");
		propLocalNameBlacklist.add("extractioncontainerfaunapop");
		propLocalNameBlacklist.add("extractioncontainerlandscape");
		propLocalNameBlacklist.add("extractioncontainersite");
		propLocalNameBlacklist.add("extractioncontainersoil");
		propLocalNameBlacklist.add("extractioncontainervegcomm");
		propLocalNameBlacklist.add("extractioncontainervegindiv");
		propLocalNameBlacklist.add("extractioncontainervegpop");
		propLocalNameBlacklist.add("geology");
		propLocalNameBlacklist.add("icons");
		propLocalNameBlacklist.add("identifier");
		propLocalNameBlacklist.add("indexnames");
		propLocalNameBlacklist.add("methods");
		propLocalNameBlacklist.add("observer");
		propLocalNameBlacklist.add("observerorganisation");
		propLocalNameBlacklist.add("organisations");
		propLocalNameBlacklist.add("overplot");
		propLocalNameBlacklist.add("plotphotograph");
		propLocalNameBlacklist.add("surveyidentifier");
		propLocalNameBlacklist.add("studylocationestablishmentmethod");
		propLocalNameBlacklist.add("studylocationdescription");
		propLocalNameBlacklist.add("themegroup");
		propLocalNameBlacklist.add("underplot");
	}
}
