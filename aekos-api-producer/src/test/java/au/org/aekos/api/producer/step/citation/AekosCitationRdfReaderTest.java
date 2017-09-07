package au.org.aekos.api.producer.step.citation;

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

import au.org.aekos.api.producer.ResourceStringifier;
import au.org.aekos.api.producer.step.citation.in.InputCitationRecord;

public class AekosCitationRdfReaderTest {

	/**
	 * Can we map the solution when everything is present?
	 */
	@Test
	public void testMapSolution01() throws Throwable {
		AekosCitationRdfReader objectUnderTest = new AekosCitationRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("urn:someGraph");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/citation/CitationRecord.ttl");
		m.read(in, null, "TURTLE");
		String sparql = new ResourceStringifier("au/org/aekos/api/producer/step/citation/testMapSolution01.rq").getValue();
		Query query = QueryFactory.create(sparql);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, ds)) {
			ResultSet queryResults = qexec.execSelect();
			QuerySolution solution = queryResults.next();
			InputCitationRecord result = objectUnderTest.mapSolution(solution);
			assertThat(result.getSamplingProtocol(), is("\"protocol 1\""));
			assertThat(result.getBibliographicCitation(), is("\"citation 1\""));
			assertThat(result.getDatasetName(), is("\"dataset 1\""));
			assertThat(result.getLicenceUrl(), is("\"http://example.com/licence\""));
		}
	}

	/**
	 * Can we read a single record (the simplest case)?
	 */
	@Test
	public void testRead01() throws Throwable {
		AekosCitationRdfReader objectUnderTest = new AekosCitationRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("http://www.aekos.org.au/ontology/1.0.0/test_project#");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/citation/surveySubgraph.ttl");
		m.read(in, null, "TURTLE");
		objectUnderTest.setDs(ds);
		String citationRecordsQuery = new ResourceStringifier("au/org/aekos/api/producer/sparql/citation-details.rq").getValue();
		objectUnderTest.setCitationDetailsQuery(citationRecordsQuery);
		InputCitationRecord result = objectUnderTest.read();
		assertThat(result.getSamplingProtocol(), is("\"aekos.org.au/collection/nsw.gov.au/nsw_atlas/vis_flora_module/MER_W2B_11\""));
		assertThat(result.getBibliographicCitation(), is("\"New South Wales Office of Environment and Heritage (2014). MER_W2B_11 Vegetation Survey, Data from the Atlas of NSW database: VIS flora survey module, Version 11/2013. Persistent URL: http://www.aekos.org.au/collection/nsw.gov.au/nsw_atlas/vis_flora_module/MER_W2B_11. TERN &#198;KOS, rights owned by State of New South Wales (Office of Environment and Heritage http://www.environment.nsw.gov.au/). Accessed [dd mmm yyyy, e.g. 01 Jan 2016].\""));
		assertThat(result.getDatasetName(), is("\"MER_W2B_11\""));
		assertThat(result.getLicenceUrl(), is("\"http://creativecommons.org/licences/by/3.0/au/deed.en\""));
	}
}
