package au.org.aekos.service.search;

import org.springframework.stereotype.Service;

import au.org.aekos.service.vocab.VocabService;

@Service
public class StubVocabService implements VocabService {

	@Override
	public String getLabelForPropertyCode(String code) {
		return "Some Label";
	}
}
