package au.org.aekos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import au.org.aekos.service.metric.IActuatorMetricService;

@Controller
public class RootController {
	
    @Autowired
    private IActuatorMetricService actMetricService;
    
	@RequestMapping(path="/doco")
    public ModelAndView root() {
		return new ModelAndView("redirect:/swagger-ui.html");
	}
	
	@RequestMapping(path="/")
    public String home() {
		return "home";
	}

	
	// Q for Tom - 
	// Should this set of methods be moved to the Metrics controller? They seem to not serve static content when there.

	// The system metrics (not usage, but threads etc)
	@RequestMapping(path="/metrics")
    public String metrics() {
		return "metrics";
	}

	// The metrics UI
	@RequestMapping(path="/metrics_dashboard")
    public String metrics_view() {
		return "metrics_dashboard";
	}
	
}
