package au.org.aekos.service.retrieval;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

import au.org.aekos.model.EnvironmentDataRecord;
import au.org.aekos.model.SpeciesOccurrenceRecord;
import au.org.aekos.model.TraitDataResponse;

@Service
public class JenaRetrievalService implements RetrievalService {

	static final String SCIENTIFIC_NAME_PLACEHOLDER = "%SCIENTIFIC_NAME_PLACEHOLDER%";
	private Model darwinCoreGraph;
	private String darwinCoreQueryTemplate;
	
	@Override
	public List<SpeciesOccurrenceRecord> getSpeciesDataJson(List<String> speciesNames, Integer limit) throws AekosApiRetrievalException {
		List<SpeciesOccurrenceRecord> result = new LinkedList<>();
		String sparql = getProcessedSparql(speciesNames);
		Query query = QueryFactory.create(sparql);
		int rowCount = 0;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, darwinCoreGraph)) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				throw new RuntimeException("No results were returned in the solution for the query: " + darwinCoreQueryTemplate);
			}
			for (; results.hasNext();) {
				boolean isLimitEnabled = limit > 0;
				boolean isLimitReached = rowCount++ >= limit;
				if (isLimitEnabled && isLimitReached) {
					break;
				}
				QuerySolution s = results.next();
				result.add(new SpeciesOccurrenceRecord(getDouble(s, "decimalLatitude"),
						getDouble(s, "decimalLongitude"), getString(s, "geodeticDatum"), getString(s, "locationID"),
						getString(s, "scientificName"), getInt(s, "individualCount"), getString(s, "eventDate"),
						getInt(s, "year"), getInt(s, "month"), getString(s, "bibliographicCitation"),
						getString(s, "datasetID")));
			}
		}
		return result;
	}

	String getProcessedSparql(List<String> speciesNames) {
		String scientificNameValueList = speciesNames.stream().collect(Collectors.joining("\" \"", "\"", "\""));
		String processedSparql = darwinCoreQueryTemplate.replace(SCIENTIFIC_NAME_PLACEHOLDER, scientificNameValueList);
		return processedSparql;
	}

	private int getInt(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getInt();
	}

	private String getString(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getString();
	}

	private double getDouble(QuerySolution soln, String variableName) {
		return soln.get(variableName).asLiteral().getDouble();
	}

	@Override
	public void getSpeciesDataCsv(List<String> speciesNames, Integer limit, boolean triggerDownload,
			Writer responseWriter) throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<EnvironmentDataRecord> getEnvironmentalData(List<String> speciesNames,
			List<String> environmentalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TraitDataResponse getTraitData(List<String> speciesNames, List<String> traitNames, int start, int count)
			throws AekosApiRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDarwinCoreGraph(Model darwinCoreGraph) {
		this.darwinCoreGraph = darwinCoreGraph;
	}

	public void setDarwinCoreQueryTemplate(String darwinCoreQueryTemplate) {
		this.darwinCoreQueryTemplate = darwinCoreQueryTemplate;
	}
}
