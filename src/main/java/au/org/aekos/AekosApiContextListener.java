package au.org.aekos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AekosApiContextListener {

	private static final Logger logger = LoggerFactory.getLogger(AekosApiContextListener.class);
	
	@EventListener({ ContextRefreshedEvent.class })
	void contextRefreshedEvent() {
		logger.info("App available at http://localhost:8099/v1/getTraitVocab.json");
	}
}
