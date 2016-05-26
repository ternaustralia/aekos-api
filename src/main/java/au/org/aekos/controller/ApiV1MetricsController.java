package au.org.aekos.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import au.org.aekos.service.metric.IActuatorMetricService;
import au.org.aekos.service.metric.IMetricService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "AekosV1", produces=MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/v1")
public class ApiV1MetricsController {

	// TODO add lots more Swagger doco
	// TODO figure out how to get Swagger to support content negotiation with overloaded methods
	// TODO am I doing content negotiation correctly?
	// TODO define coord ref system
	// TODO do we accept LSID/species ID and/or a species name for the species related services?

	@Autowired
	private IMetricService metricService;

    @RequestMapping(value = "/metric-graph-data", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get the system's current metrics data", notes = "TODO", tags="Metrics")
    @ResponseBody
    public Object[][] getMetricData() {
        return metricService.getGraphData();
    }    
    
    /*
    @RequestMapping(value = "/status_metric", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get the system's current status metric data", notes = "TODO", tags="Metrics")
    @ResponseBody
    public Map<Integer, Integer> getStatusMetric() {
        return metricService.getStatusMetric();
    }    
    
    @RequestMapping(value = "/full_metric", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get the system's current status metric data", notes = "TODO", tags="Metrics")
    @ResponseBody
    public Map<String, ConcurrentHashMap<Integer, Integer>> getFullMetric() {
        return metricService.getFullMetric();
    }    
    */
    
}
