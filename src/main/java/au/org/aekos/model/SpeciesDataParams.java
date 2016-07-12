package au.org.aekos.model;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import au.org.aekos.service.metric.JenaMetricsStorageService;

public class SpeciesDataParams extends AbstractParams {
	private static final String SPECIES_NAMES_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramSpeciesNames";
	
	private final List<String> speciesNames;

	public SpeciesDataParams(int start, int rows, List<String> speciesNames) {
		super(start, rows);
		this.speciesNames = speciesNames;
	}

	public List<String> getSpeciesNames() {
		return speciesNames;
	}

	@Override
	void subAppendTo(Resource subject, Model metricsModel) {
		Property speciesNamesProp = metricsModel.createProperty(SPECIES_NAMES_PROP);
		for (String curr : speciesNames) {
			metricsModel.add(subject, speciesNamesProp, curr);
		}
	}
}