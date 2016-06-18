package au.org.aekos.service.auth;

import org.springframework.util.StringUtils;

public class AekosApiAuthKey {

	private final String keyStringValue;

	public AekosApiAuthKey(String keyStringValue) throws InvalidKeyException {
		verify(keyStringValue);
		this.keyStringValue = keyStringValue;
	}

	private void verify(String potentialKey) throws InvalidKeyException {
		if (StringUtils.isEmpty(potentialKey)) {
			throw new InvalidKeyException("supplied key is blank/empty/null");
		}
		// TODO verify valid format
	}

	public String getKeyStringValue() {
		return keyStringValue;
	}

	public static class InvalidKeyException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidKeyException(String message) {
			super("AEKOS API Key problem: " + message);
		}
	}
}
