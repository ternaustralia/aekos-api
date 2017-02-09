package au.org.aekos.api.service.vocab;

public interface VocabService {

	/**
	 * Gets the label (AKA title) for the datatype property identified by the
	 * supplied local name.
	 * 
	 * @param code	local name of the property
	 * @return		label if found, null otherwise
	 */
	String getLabelForPropertyCode(String code);
}
