package au.org.aekos.service.metric;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidIdProvider implements IdProvider {

	@Override
	public String nextId() {
		return "urn:" + UUID.randomUUID().toString();
	}

}
