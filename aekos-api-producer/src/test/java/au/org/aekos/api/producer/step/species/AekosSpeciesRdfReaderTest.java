package au.org.aekos.api.producer.step.species;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
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
			assertThat(result.getRdfSubject(), is("urn:test#record1"));
			assertThat(result.getIndividualCount(), is(1));
			assertThat(result.getEventDate(), is("2007-09-29"));
			assertThat(result.getLocationID(), is("location1"));
			assertThat(result.getScientificName(), is("name1"));
			assertThat(result.getTaxonRemarks(), is("remarks1"));
		}
	}

	/**
	 * Can we read five visit records from two visits?
	 */
	@Test
	public void testRead02() throws Throwable {
		AekosSpeciesRdfReader objectUnderTest = new AekosSpeciesRdfReader();
		Dataset ds = DatasetFactory.create();
		Model m = ds.getNamedModel("http://www.aekos.org.au/ontology/1.0.0/test_project#");
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("au/org/aekos/api/producer/step/species/fiveSpeciesTwoVisits.ttl");
		m.read(in, null, "TURTLE");
		objectUnderTest.setDs(ds);
		String speciesRecordsQuery = Utils.getResourceAsString("au/org/aekos/api/producer/sparql/species-records.rq");
		objectUnderTest.setSpeciesRecordsQuery(speciesRecordsQuery);
		{
			InputSpeciesRecord nextResult = objectUnderTest.read();
			assertThat(nextResult.getId().length(), is(36));
			assertThat(nextResult.getLocationID(), is("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054"));
			assertThat(nextResult.getIndividualCount(), is(1));
			assertThat(nextResult.getEventDate(), is("2007-04-20"));
			assertThat(nextResult.getScientificName(), is("Acacia octonervia R.S.Cowan & Maslin"));
			assertNull(nextResult.getTaxonRemarks());
			assertThat(nextResult.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
			assertThat(nextResult.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test#SPECIESORGANISMGROUP-T1485141126500"));
		} {
			InputSpeciesRecord nextResult = objectUnderTest.read();
			assertThat(nextResult.getId().length(), is(36));
			assertThat(nextResult.getLocationID(), is("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054"));
			assertThat(nextResult.getIndividualCount(), is(1));
			assertThat(nextResult.getEventDate(), is("2007-09-29"));
			assertThat(nextResult.getScientificName(), is("Eutaxia cuneata Meisn."));
			assertNull(nextResult.getTaxonRemarks());
			assertThat(nextResult.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
			assertThat(nextResult.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test#SPECIESORGANISMGROUP-T1485141126873"));
		} {
			InputSpeciesRecord nextResult = objectUnderTest.read();
			assertThat(nextResult.getId().length(), is(36));
			assertThat(nextResult.getLocationID(), is("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054"));
			assertThat(nextResult.getIndividualCount(), is(1));
			assertThat(nextResult.getEventDate(), is("2007-04-20"));
			assertThat(nextResult.getScientificName(), is("Dampiera lavandulacea Lindl."));
			assertNull(nextResult.getTaxonRemarks());
			assertThat(nextResult.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
			assertThat(nextResult.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test#SPECIESORGANISMGROUP-T1485141126538"));
		} {
			InputSpeciesRecord nextResult = objectUnderTest.read();
			assertThat(nextResult.getId().length(), is(36));
			assertThat(nextResult.getLocationID(), is("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054"));
			assertThat(nextResult.getIndividualCount(), is(1));
			assertThat(nextResult.getEventDate(), is("2007-09-29"));
			assertThat(nextResult.getScientificName(), is("Acacia octonervia R.S.Cowan & Maslin"));
			assertNull(nextResult.getTaxonRemarks());
			assertThat(nextResult.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
			assertThat(nextResult.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test#SPECIESORGANISMGROUP-T1485141126864"));
		} {
			InputSpeciesRecord nextResult = objectUnderTest.read();
			assertThat(nextResult.getId().length(), is(36));
			assertThat(nextResult.getLocationID(), is("aekos.org.au/collection/wa.gov.au/ravensthorpe/R054"));
			assertThat(nextResult.getIndividualCount(), is(1));
			assertThat(nextResult.getEventDate(), is("2007-04-20"));
			assertThat(nextResult.getScientificName(), is("Acacia ingrata Benth."));
			assertNull(nextResult.getTaxonRemarks());
			assertThat(nextResult.getRdfGraph(), is("http://www.aekos.org.au/ontology/1.0.0/test_project#"));
			assertThat(nextResult.getRdfSubject(), is("http://www.aekos.org.au/ontology/1.0.0/test#SPECIESORGANISMGROUP-T1485141126490"));
		}
		assertNull("Should send null as a poison pill when no more records left", objectUnderTest.read());
	}
}
