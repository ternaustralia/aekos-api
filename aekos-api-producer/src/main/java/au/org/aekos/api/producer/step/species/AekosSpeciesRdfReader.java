package au.org.aekos.api.producer.step.species;

import org.apache.jena.query.QuerySolution;

import au.org.aekos.api.producer.step.AbstractRdfReader;
import au.org.aekos.api.producer.step.SolutionVariableExtractor;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class AekosSpeciesRdfReader extends AbstractRdfReader<InputSpeciesRecord> {

	private String speciesRecordsQuery;

	@Override
	public InputSpeciesRecord mapSolution(QuerySolution solution) {
		SolutionVariableExtractor e = new SolutionVariableExtractor(solution, "rdfSubject");
		return new InputSpeciesRecord(
				e.get("id"),
				e.getResourceUri("rdfSubject"),
				e.getResourceUri("rdfGraph"),
				e.getInt("individualCount"),
				e.get("locationID"),
				e.getOptional("scientificName"),
				e.getOptional("taxonRemarks"),
				e.get("eventDate")
			);
	}
	
	@Override
	public String getRecordTypeName() {
		return "species";
	}

	@Override
	public String getSparqlQuery() {
		return speciesRecordsQuery;
	}
	
	public void setSpeciesRecordsQuery(String speciesRecordQuery) {
		this.speciesRecordsQuery = speciesRecordQuery;
	}
}
