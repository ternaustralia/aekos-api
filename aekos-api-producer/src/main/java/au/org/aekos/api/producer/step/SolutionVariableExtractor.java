package au.org.aekos.api.producer.step;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

public class SolutionVariableExtractor {
	private QuerySolution solution;
	private String idVariableName;

	public SolutionVariableExtractor(QuerySolution solution, String idVariableName) {
		this.solution = solution;
		this.idVariableName = idVariableName;
	}

	public String get(String variableName) {
		return getLiteral(variableName).getString();
	}

	public String getResourceUri(String variableName) {
		return solution.getResource(variableName).getURI();
	}

	public String getOptional(String variableName) {
		RDFNode node = solution.get(variableName);
		Literal literal = asLiteral(node, variableName);
		if (literal == null) {
			return null;
		}
		return literal.getString();
	}

	public double getDouble(String variableName) {
		return getLiteral(variableName).getDouble();
	}

	public int getInt(String variableName) {
		return getLiteral(variableName).getInt();
	}

	private Literal getLiteral(String variableName) {
		RDFNode node = solution.get(variableName);
		if (node == null) {
			Iterable<String> iterable = () -> solution.varNames();
			Set<String> vars = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toSet());
			String template = "Data problem: in record ID '%s' couldn't find the variable '%s', only found the vars %s";
			String recordId;
			try {
				recordId = getResourceUri(idVariableName);
			} catch (Exception e) {
				recordId = "(Error: failed to get record id. " + e.getMessage() + ")";
			}
			throw new MissingDataException(String.format(template, recordId, variableName, vars));
		}
		return asLiteral(node, variableName);
	}

	private Literal asLiteral(RDFNode node, String variableName) {
		if (node == null) {
			return null;
		}
		if (!node.isLiteral()) {
			String template = "Data problem: expected variable '%s' to be a literal but it was a '%s'.";
			throw new IllegalStateException(String.format(template, variableName, node.getClass().getName()));
		}
		return node.asLiteral();
	}
}