package au.org.aekos.api.service.metric;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidIdProvider implements IdProvider {

	@Override
	public String nextId() {
		return "urn:" + UUID.randomUUID().toString();
	}

}
