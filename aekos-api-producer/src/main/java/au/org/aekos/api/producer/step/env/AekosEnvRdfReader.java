package au.org.aekos.api.producer.step.env;

import org.apache.jena.query.QuerySolution;

import au.org.aekos.api.producer.step.AbstractRdfReader;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class AekosEnvRdfReader extends AbstractRdfReader<InputEnvRecord> {

	private String environmentalVariableQuery;

	@Override
	public InputEnvRecord mapSolution(QuerySolution solution) {
		Extractor e = new Extractor(solution, "locationID");
		return new InputEnvRecord(
				e.get("locationID"),
				e.getDouble("decimalLatitude"),
				e.getDouble("decimalLongitude"),
				e.get("geodeticDatum"),
				e.get("locationName")
			);
	}
	
	@Override
	public String getRecordTypeName() {
		return "env-var";
	}

	@Override
	public String getSparqlQuery() {
		return environmentalVariableQuery;
	}

	public void setEnvironmentalVariableQuery(String environmentalVariableQuery) {
		this.environmentalVariableQuery = environmentalVariableQuery;
	}
}
