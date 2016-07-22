package au.org.aekos.service.auth;

/**
 * Service to persist and manage API keys.
 */
public interface AuthStorageService {

	public enum SignupMethod {
		EMAIL,
		GOOGLE,
		FACEBOOK;
	}
	
	/**
	 * Persists the key with the supplied details
	 * 
	 * @param emailAddress	registered email address of the key
	 * @param key			key itself
	 * @param signupMethod	how the user signed up
	 */
	void storeNewKey(String emailAddress, AekosApiAuthKey key, SignupMethod signupMethod);
	
	/**
	 * Checks if the supplied key is valid
	 * 
	 * @param key	key to check
	 * @return		<code>true</code> if the supplied code is valid, <code>false</code> otherwise
	 */
	boolean isValidKey(AekosApiAuthKey key);

	/**
	 * Disables the supplied key.
	 * 
	 * Will have no effect if the supplied key is already disabled or doesn't exist.
	 * 
	 * @param key	key to disable
	 */
	void disableKey(AekosApiAuthKey key);

	/**
	 * Enables the supplied key.
	 * 
	 * Will have no effect if the supplied key is already enabled or doesn't exist.
	 * 
	 * @param key	key to enable
	 */
	void enableKey(AekosApiAuthKey key);
	
	/**
	 * Checks if the supplied key exists in the store.
	 * 
	 * @param key	key to check
	 * @return		<code>true</code> if the supplied key exists, <code>false</code> otherwise
	 */
	boolean exists(AekosApiAuthKey key);
	
	/**
	 * Gets a summary of keys
	 * 
	 * @return key summary
	 */
	KeySummary getSummary();
	
	class KeySummary {
		private final int enabledCount;
		private final int disabledCount;
		
		public KeySummary(int enabledCount, int disabledCount) {
			this.enabledCount = enabledCount;
			this.disabledCount = disabledCount;
		}
		public int getEnabledCount() {
			return enabledCount;
		}
		public int getDisabledCount() {
			return disabledCount;
		}
		public int getTotalCount() {
			return disabledCount + enabledCount;
		}
	}
}
