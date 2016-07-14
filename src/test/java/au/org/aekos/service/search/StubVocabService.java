package au.org.aekos.service.search;

import au.org.aekos.service.vocab.VocabService;

public class StubVocabService implements VocabService {

	@Override
	public String getLabelForPropertyCode(String code) {
		return "Some Label";
	}
}
