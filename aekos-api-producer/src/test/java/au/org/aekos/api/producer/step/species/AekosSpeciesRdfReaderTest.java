package au.org.aekos.api.producer.step.species;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import au.org.aekos.api.producer.Utils;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class AekosSpeciesRdfReaderTest {

	/**
	 * Can we map the solution when everything is present?
	 */
	@Test
	public void testMapSolution01() throws Throwable {
		AekosSpeciesRdfReader objectUnderTest = new AekosSpeciesRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("urn:someGraph");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/species/SpeciesRecord.ttl");
		m.read(in, null, "TURTLE");
		String sparql = Utils.getResourceAsString("au/org/aekos/api/producer/step/species/testMapSolution01.rq");
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet queryResults = qexec.execSelect();
			QuerySolution solution = queryResults.next();
			InputSpeciesRecord result = objectUnderTest.mapSolution(solution);
			assertThat(result.getId(), is("id1"));
			assertThat(result.getRdfGraph(), is("urn:someGraph"));
			assertThat(result.getRdfSubject(), is("urn:record1"));
			assertThat(result.getIndividualCount(), is(1));
			assertThat(result.getLocationID(), is("location1"));
			assertThat(result.getScientificName(), is("name1"));
			assertThat(result.getTaxonRemarks(), is("remarks1"));
		}
	}

}
