package au.org.aekos.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import au.org.aekos.service.metric.JenaMetricsStorageService;

public abstract class AbstractParams {
	public static final String START_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramStart";
	public static final String ROWS_PROP = JenaMetricsStorageService.METRICS_NAMESPACE_V1_0 + "paramRows";
	
	private final int start;
	private final int rows;

	public AbstractParams(int start, int rows) {
		this.start = start;
		this.rows = rows;
	}

	public int getStart() {
		return start;
	}

	public int getRows() {
		return rows;
	}

	public void appendTo(Resource subject, Model metricsModel) {
		Property startProp = metricsModel.createProperty(START_PROP);
		metricsModel.addLiteral(subject, startProp, start);
		Property rowsProp = metricsModel.createProperty(ROWS_PROP);
		metricsModel.addLiteral(subject, rowsProp, rows);
		subAppendTo(subject, metricsModel);
	}
	
	abstract void subAppendTo(Resource subject, Model metricsModel);
}