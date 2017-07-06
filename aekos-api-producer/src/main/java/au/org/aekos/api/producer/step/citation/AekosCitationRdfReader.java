package au.org.aekos.api.producer.step.citation;

import org.apache.jena.query.QuerySolution;

import au.org.aekos.api.producer.step.AbstractRdfReader;
import au.org.aekos.api.producer.step.SolutionVariableExtractor;
import au.org.aekos.api.producer.step.citation.in.InputCitationRecord;

public class AekosCitationRdfReader extends AbstractRdfReader<InputCitationRecord> {

	private String citationDetailsQuery;

	@Override
	public InputCitationRecord mapSolution(QuerySolution solution) {
		SolutionVariableExtractor e = new SolutionVariableExtractor(solution, "samplingProtocol");
		return new InputCitationRecord(
				e.get("samplingProtocol"),
				e.get("bibliographicCitation"),
				e.get("datasetName")
			);
	}

	@Override
	public String getRecordTypeName() {
		return "citation";
	}

	@Override
	public String getSparqlQuery() {
		return citationDetailsQuery;
	}

	public void setCitationDetailsQuery(String citationDetailsQuery) {
		this.citationDetailsQuery = citationDetailsQuery;
	}
}
