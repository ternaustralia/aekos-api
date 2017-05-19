package au.org.aekos.api.producer.step.env;

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
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class AekosEnvRdfReaderTest {

	/**
	 * Can we map the solution when everything is present?
	 */
	@Test
	public void testMapSolution01() throws Throwable {
		AekosEnvRdfReader objectUnderTest = new AekosEnvRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("urn:someGraph");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/env/EnvRecord.ttl");
		m.read(in, null, "TURTLE");
		String sparql = Utils.getResourceAsString("au/org/aekos/api/producer/step/env/testMapSolution01.rq");
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet queryResults = qexec.execSelect();
			QuerySolution solution = queryResults.next();
			InputEnvRecord result = objectUnderTest.mapSolution(solution);
			assertThat(result.getRdfGraph(), is("urn:someGraph"));
			assertThat(result.getRdfSubject(), is("urn:record1"));
			assertThat(result.getDecimalLatitude(), is(-42.682963));
			assertThat(result.getDecimalLongitude(), is(146.649282));
			assertThat(result.getGeodeticDatum(), is("GDA94"));
			assertThat(result.getEventDate(), is("1990-03-30"));
			assertThat(result.getMonth(), is(3));
			assertThat(result.getYear(), is(1990));
			assertThat(result.getLocationID(), is("location1"));
			assertThat(result.getLocationName(), is("locName1"));
			assertThat(result.getSamplingProtocol(), is("sampling1"));
		}
	}

	/**
	 * Can we read a single record (the simplest case)?
	 */
	@Test
	public void testRead01() throws Throwable {
		AekosEnvRdfReader objectUnderTest = new AekosEnvRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("http://www.aekos.org.au/ontology/1.0.0/test_project#");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/env/singleLocSingleVisit.ttl");
		m.read(in, null, "TURTLE");
		objectUnderTest.setDs(ds);
		String environmentalVariableQuery = Utils.getResourceAsString("au/org/aekos/api/producer/sparql/environmental-variables.rq");
		objectUnderTest.setEnvironmentalVariableQuery(environmentalVariableQuery);
		InputEnvRecord result = objectUnderTest.read();
		assertThat(result.getLocationID(), is("aekos.org.au/collection/sydney.edu.au/DERG/Main Camp"));
		assertThat(result.getDecimalLatitude(), is(-23.5318398476576));
		assertThat(result.getDecimalLongitude(), is(138.321378247854));
		assertThat(result.getGeodeticDatum(), is("GDA94"));
		assertThat(result.getEventDate(), is("1990-03-30"));
		assertThat(result.getMonth(), is(3));
		assertThat(result.getYear(), is(1990));
		assertThat(result.getLocationName(), is("Main Camp"));
		assertThat(result.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
		assertThat(result.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#STUDYLOCATIONVISITVIEW-T1493794229804"));
		assertThat(result.getSamplingProtocol(), is("aekos.org.au/collection/sydney.edu.au/DERG"));
	}
	
	// TODO test that having both options for location doesn't cause dupes
}
