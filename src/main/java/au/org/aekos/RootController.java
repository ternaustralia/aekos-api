package au.org.aekos;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RootController {

	@RequestMapping(path="/doco")
    public ModelAndView root() {
		return new ModelAndView("redirect:/swagger-ui.html");
	}
	
	@RequestMapping(path="/")
    public String home() {
		return "home";
	}
}
