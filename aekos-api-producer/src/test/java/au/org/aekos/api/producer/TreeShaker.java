package au.org.aekos.api.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

/**
 * Loads a TTL file, removes all dupes (actually get this for free) and ophans
 * and writes the clean file out.
 */
public class TreeShaker {

	public static void main(String[] args) throws Throwable {
		Model m = loadModel();
		Resource fullRootSubject = m.createResource("http://www.aekos.org.au/ontology/1.0.0/test_project#STUDYLOCATIONSUBGRAPH-T1493794229549"); // make sure you update this before running
		recursivelyTreeShake(m, fullRootSubject);
		fixBagNumbering(m);
		write(m);
	}

	private static Model loadModel() {
		Model m = ModelFactory.createDefaultModel();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/env/singleLocTwoVisit.ttl");
		m.read(in, null, "TURTLE");
		return m;
	}

	private static void recursivelyTreeShake(Model m, Resource fullRootSubject) {
		while (true) {
			Set<Resource> allSubjects = m.listSubjects().toSet();
			Set<Resource> subjectsToRemove = new HashSet<>(allSubjects);
			subjectsToRemove.remove(fullRootSubject); // always leave the root
			for (NodeIterator it = m.listObjects(); it.hasNext();) {
				RDFNode next = it.next();
				if (!next.isResource()) {
					continue;
				}
				subjectsToRemove.remove(next.asResource());
			}
			boolean noItemsToRemove = subjectsToRemove.isEmpty();
			if (noItemsToRemove) {
				break;
			}
			for (Resource curr : subjectsToRemove) {
				m.removeAll(curr, null, null);
			}
		}
	}
	
	/**
	 * Re-adds all the bag elements so they have sequential indicies after we've hacked them up
	 */
	private static void fixBagNumbering(Model m) {
		ResIterator allBags = m.listResourcesWithProperty(RDF.type, RDF.Bag);
		while (allBags.hasNext()) {
			Bag currBag = allBags.next().as(Bag.class);
			List<Statement> allBagStatements = currBag.listProperties().toList();
			currBag.removeProperties();
			currBag.addProperty(RDF.type, RDF.Bag);
			for (Statement currStatement : allBagStatements) {
				if (currStatement.getPredicate().getURI().equals(RDF.type.getURI())) {
					continue;
				}
				currBag.add(currStatement.getObject());
			}
		}
	}
	
	private static void write(Model m) throws IOException, FileNotFoundException {
		Path outputFile = Files.createTempFile("cleanedRdf", ".ttl");
		m.write(new FileOutputStream(outputFile.toFile()), "TURTLE");
		System.out.println("Wrote cleaned file to: " + outputFile.toString());
	}
	
	/**
	 * Can we fix up a bag with inconsistent indicies? 
	 */
	@Test
	public void testFixBagNumbering() {
		Model m = ModelFactory.createDefaultModel();
		String bagTtl =
			"PREFIX : <urn:test#> " +
			":theBag " +
			  "a       <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ; " +
			  "<http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> \"one\" ; " +
			  "<http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> \"four\" " +
			" .";
		m.read(new StringReader(bagTtl), null, "TURTLE");
		fixBagNumbering(m);
		Bag bag = m.createResource("urn:test#theBag").as(Bag.class);
		assertThat(bag.getProperty(RDF.type).getResource(), is(RDF.Bag));
		NodeIterator it = bag.iterator();
		assertThat(it.next().asLiteral().getString(), is("four"));
		assertThat(it.next().asLiteral().getString(), is("one"));
		assertFalse(it.hasNext());
	}
	
	/**
	 * Does a bag with inconsistent indicies iterate as poorly as we expect?
	 */
	@Test
	public void testCanIterateMessedUpBagIndicies() {
		Model m = ModelFactory.createDefaultModel();
		String bagTtl =
			"PREFIX : <urn:test#> " +
			":theBag " +
			  "a       <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ; " +
			  "<http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> \"one\" ; " +
			  "<http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> \"four\" " +
			" .";
		m.read(new StringReader(bagTtl), null, "TURTLE");
		Bag bag = m.createResource("urn:test#theBag").as(Bag.class);
		NodeIterator it = bag.iterator();
		assertThat(it.next().asLiteral().getString(), is("one"));
		try {
			assertThat(it.next().asLiteral().getString(), is("four")); // this fails because it's not index #2
		} catch (NullPointerException e) {
			// success... well, expected at least
		}
	}
}
