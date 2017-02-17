package au.org.aekos.api.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * Generates test data. Starting from the supplied root resource, we grab the entire subgraph
 * and apply filters (max bag sizes, property name blacklists, etc) to generate a useable sized
 * test dataset.
 * 
 * It works with a ~100mb input dataset on a 8GB machine. It struggles with 700mb.
 */
public class RdfSubgraphDataExtractorThingy {

	private static final String SEPARATOR = "|";
	private static final String AEKOS = "http://www.aekos.org.au/ontology/1.0.0#";
	private static final String COM = "http://www.aekos.org.au/ontology/1.0.0/aekos_common#";
	private static final String WAD = "http://www.aekos.org.au/ontology/1.0.0/wadec_ravensthorpe#";
	private static final String ANY_TYPE = "*";
	// Configure this stuff \/
	private static final String SOURCE_TTL_FILE = "/data/all2.ttl";
	private static final String OUTPUT_TTL_FILE = "/data/subgraph.ttl";
	private static final int MAX_BAG_ELEMENTS = 8;
	private static final String ROOT_RESOURCE = WAD + "STUDYLOCATIONSUBGRAPH-T1485141126269";
	private static final boolean RECREATE_TDB = true;
	// Configure this stuff /\
	private final Set<Resource> seen = new HashSet<>();
	private final Set<String> propLocalNameBlacklist = new HashSet<>();
	
	public static void main(String[] args) throws Throwable {
		System.out.println("Running");
		new RdfSubgraphDataExtractorThingy().run();
		System.out.println("Done");
	}

	private void run() throws Throwable {
		initBlacklist();
		String tdbDir = "/tmp/subgraph-dumper-thingy-tdb";
		if (RECREATE_TDB) {FileUtils.deleteDirectory(new File(tdbDir));}
		Dataset ds = TDBFactory.createDataset(tdbDir);
		Model source = ds.getDefaultModel();
		if (RECREATE_TDB) {
			System.out.println("Loading data...");
			source.read(new FileInputStream(SOURCE_TTL_FILE), null, "TURTLE");
		}
		System.out.println("Processing data...");
		Model output = ModelFactory.createDefaultModel();
		output.setNsPrefix("", AEKOS);
		output.setNsPrefix("com", COM);
		output.setNsPrefix("wad", WAD);
		Resource slsg = source.getResource(ROOT_RESOURCE);
		processResource(slsg, output);
		output.write(new FileOutputStream(OUTPUT_TTL_FILE), "TURTLE");
		ds.close();
		if (RECREATE_TDB) {FileUtils.deleteDirectory(new File(tdbDir));}
	}

	private void processResource(Resource res, Model output) {
		if (!seen.add(res)) {
			return;
		}
		int bagCounter = 0;
		for (StmtIterator it = res.listProperties(); it.hasNext();) {
			Statement curr = it.next();
			String currPredLocalName = curr.getPredicate().getLocalName();
			String subjectType = curr.getSubject().getProperty(RDF.type).getObject().asResource().getLocalName();
			if (propLocalNameBlacklist.contains(ANY_TYPE + SEPARATOR + currPredLocalName)
					|| propLocalNameBlacklist.contains(subjectType + SEPARATOR + currPredLocalName)) {
				continue;
			}
			if (res.hasProperty(RDF.type, RDF.Bag)) {
				if (++bagCounter > MAX_BAG_ELEMENTS) {
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
		Collection<String> anyParentTypeBlacklistProperties = Arrays.asList(
			"abstract",
			"conditions",
			"detail",
			"entitydisplay",
			"extractioncontainer",
			"extractioncontainerfaunaindiv",
			"extractioncontainerfaunapop",
			"extractioncontainerlandscape",
			"extractioncontainersite",
			"extractioncontainersoil",
			"extractioncontainervegcomm",
			"extractioncontainervegindiv",
			"extractioncontainervegpop",
			"geology",
			"icons",
			"identifier",
			"indexnames",
			"methods",
			"observer",
			"observerorganisation",
			"organisations",
			"overplot",
			"plotphotograph",
			"surveyidentifier",
			"studylocationestablishmentmethod",
			"studylocationdescription",
			"themegroup",
			"underplot");
		for (String curr : anyParentTypeBlacklistProperties) {
			propLocalNameBlacklist.add(ANY_TYPE + SEPARATOR + curr);
		}
		propLocalNameBlacklist.add("STUDYLOCATIONSUBGRAPH" + SEPARATOR + "observeditems");
	}
}
