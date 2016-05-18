package au.org.aekos;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class RootController {

	@RequestMapping(path="/", method=RequestMethod.GET)
    public ModelAndView speciesAutocomplete() {
		return new ModelAndView("redirect:/swagger-ui.html");
	}
}
