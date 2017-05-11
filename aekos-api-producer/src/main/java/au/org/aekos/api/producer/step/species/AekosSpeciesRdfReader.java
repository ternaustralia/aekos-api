package au.org.aekos.api.producer.step.species;

import org.apache.jena.query.QuerySolution;

import au.org.aekos.api.producer.step.AbstractRdfReader;
import au.org.aekos.api.producer.step.species.in.InputSpeciesRecord;

public class AekosSpeciesRdfReader extends AbstractRdfReader<InputSpeciesRecord> {

	private String dwcAndTraitsQuery;

	@Override
	public InputSpeciesRecord mapSolution(QuerySolution solution) {
		Extractor e = new Extractor(solution, "scientificName"/*FIXME this isn't specific enough to find issues*/);
		return new InputSpeciesRecord(
				e.get("id"),
				e.getResourceUri("rdfSubject"),
				e.getResourceUri("rdfGraph"),
				e.getInt("individualCount"),
				e.get("locationID"),
				e.getOptional("scientificName"),
				e.getOptional("taxonRemarks")
			);
	}
	
	@Override
	public String getRecordTypeName() {
		return "species";
	}

	@Override
	public String getSparqlQuery() {
		return dwcAndTraitsQuery;
	}
	
	public void setDwcAndTraitsQuery(String dwcAndTraitsQuery) {
		this.dwcAndTraitsQuery = dwcAndTraitsQuery;
	}
}
