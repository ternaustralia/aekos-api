package au.org.aekos.model;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import au.org.aekos.service.metric.JenaMetricsStorageService;

public class TraitDataParams extends SpeciesDataParams {
	private static final String TRAITS_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramTraits";
	
	private final List<String> traitNames;

	public TraitDataParams(int start, int rows, List<String> speciesNames, List<String> traitNames) {
		super(start, rows, speciesNames);
		this.traitNames = traitNames;
	}

	public List<String> getTraitNames() {
		return traitNames;
	}
	
	@Override
	void subAppendTo(Resource subject, Model metricsModel) {
		super.subAppendTo(subject, metricsModel);
		Property startProp = metricsModel.createProperty(TRAITS_PROP);
		for (String curr : traitNames) {
			metricsModel.add(subject, startProp, curr);
		}
	}
}