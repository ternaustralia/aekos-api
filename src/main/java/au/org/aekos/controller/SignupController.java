package au.org.aekos.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.github.mkopylec.recaptcha.validation.ValidationResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "AekosV1", produces=MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/v1")
public class SignupController {

	private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    private RecaptchaValidator recaptchaValidator;

    @RequestMapping(path = "/signup", method = RequestMethod.POST)
	@ApiOperation(value = "Process the captcha result", notes = "TODO", tags="Signup")
    public ModelAndView validateCaptcha(HttpServletRequest request) {
        ValidationResult result = recaptchaValidator.validate(request);
        if (result.isSuccess()) {
        	logger.debug("Authenticated");
    		return new ModelAndView("redirect:/signedup.html?status=ok");
        } else {
        	return new ModelAndView("redirect:/signedup.html?status=failed");
        }
    }
}