package au.org.aekos.service.auth;

public interface AuthStorageService {

	public enum SignupMethod {
		EMAIL,
		GOOGLE,
		FACEBOOK;
	}
	
	void storeNewKey(String emailAddress, AekosApiAuthKey key, SignupMethod signupMethod);
	
	boolean isValidKey(AekosApiAuthKey key);

	void disableKey(AekosApiAuthKey key);

	void enableKey(AekosApiAuthKey key);
}
