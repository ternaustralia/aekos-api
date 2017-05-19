package au.org.aekos.api.producer.step.env;

import org.apache.jena.query.QuerySolution;

import au.org.aekos.api.producer.step.AbstractRdfReader;
import au.org.aekos.api.producer.step.env.in.InputEnvRecord;

public class AekosEnvRdfReader extends AbstractRdfReader<InputEnvRecord> {

	private String siteVisitRecordsQuery;

	@Override
	public InputEnvRecord mapSolution(QuerySolution solution) {
		Extractor e = new Extractor(solution, "locationID");
		return new InputEnvRecord(
				e.get("locationID"),
				e.getDouble("decimalLatitude"),
				e.getDouble("decimalLongitude"),
				e.get("geodeticDatum"),
				e.get("locationName"),
				e.get("samplingProtocol"),
				e.getResourceUri("rdfSubject"),
				e.getResourceUri("rdfGraph"),
				e.get("eventDate"),
				e.getInt("month"),
				e.getInt("year")
			);
	}
	
	@Override
	public String getRecordTypeName() {
		return "env-var";
	}

	@Override
	public String getSparqlQuery() {
		return siteVisitRecordsQuery;
	}

	public void setSiteVisitRecordsQuery(String siteVisitRecordsQuery) {
		this.siteVisitRecordsQuery = siteVisitRecordsQuery;
	}
}
