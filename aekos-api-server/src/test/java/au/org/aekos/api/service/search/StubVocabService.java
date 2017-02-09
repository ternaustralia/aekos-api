package au.org.aekos.api.service.search;

import au.org.aekos.api.service.vocab.VocabService;

public class StubVocabService implements VocabService {

	@Override
	public String getLabelForPropertyCode(String code) {
		return "Some Label";
	}
}
