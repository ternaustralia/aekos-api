package au.org.aekos.api.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class GlobalDefaultExceptionHandler {

	private static final Logger logger = Logger.getLogger(GlobalDefaultExceptionHandler.class);
	public static final String DEFAULT_ERROR_VIEW = "error";
	
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorContainer defaultErrorHandler(HttpServletRequest request, Exception e) {
        String message = "Sorry about that, something has gone wrong. It's out fault and we're looking into it.";
		logger.error(message, e);
        // TODO integrate with Rollbar or similar
        return new ErrorContainer(message, HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date().getTime());
    }
    
    static class ErrorContainer {
    	private final String message;
    	private final int statusCode;
    	private final long timestamp;
    	
		public ErrorContainer(String message, int statusCode, long timestamp) {
			this.message = message;
			this.statusCode = statusCode;
			this.timestamp = timestamp;
		}

		public String getMessage() {
			return message;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public long getTimestamp() {
			return timestamp;
		}
    }
}