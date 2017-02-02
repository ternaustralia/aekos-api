package au.org.aekos.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import au.org.aekos.api.service.metric.MetricsStorageService;

@Controller
public class RootController implements ErrorController {
	
	@Autowired
	@Qualifier("jenaMetricsStorageService")
	private MetricsStorageService metricsService;
	
	@RequestMapping(path="/doco")
    public ModelAndView root() {
		return new ModelAndView("redirect:/swagger-ui.html");
	}
	
	@RequestMapping(path="/")
    public String home() {
		return "home";
	}

	// 404
	@RequestMapping(path="/error")
    public String error() {
		return "error";
	}

    @Override
    public String getErrorPath() {
        return "/error";
    }
	
	// User requesting a key
	@RequestMapping(path="/access")
    public String access() {
		return "access";
	}
	
	@RequestMapping(path="/signedup")
    public String signedup() {
		return "signedup";
	}
	
	// The system metrics (not usage, but threads etc)
	@RequestMapping(path="/metrics")
    public String metrics() {
		return "metrics";
	}

	// The metrics UI
	@RequestMapping(path="/metrics_dashboard")
    public ModelAndView metrics_view() {
		ModelAndView result = new ModelAndView("metrics_dashboard");
		result.addObject("requestSummary", metricsService.getRequestSummary());
		return result;
	}
}
