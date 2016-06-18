package au.org.aekos.service.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.auth.AuthStorageService.SignupMethod;

public class JenaAuthStorageServiceTest {

	/**
	 * Can we store a new API key?
	 */
	@Test
	public void testStoreNewKey01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		objectUnderTest.storeNewKey("test@example.com", new AekosApiAuthKey("AAABBB123"), SignupMethod.EMAIL);
		Writer out = new StringWriter();
		authModel.write(out, "TURTLE");
		assertThat(out.toString(), is(
			"<http://www.aekos.org.au/api/1.0/auth#AAABBB123>\n" +
			"        a       <http://www.aekos.org.au/api/1.0/auth#AekosApiAuthKey> ;\n" +
			"        <http://www.aekos.org.au/api/1.0/auth#ownedBy>\n" +
			"                \"test@example.com\" ;\n" +
			"        <http://www.aekos.org.au/api/1.0/auth#signupMethod>\n" +
			"                \"EMAIL\" .\n"));
	}
	
	/**
	 * Can we tell when a key is valid?
	 */
	@Test
	public void testIsValidKey01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		objectUnderTest.storeNewKey("test@example.com", key, SignupMethod.EMAIL);
		boolean result = objectUnderTest.isValidKey(key);
		assertTrue("Should be valid because we just created it", result);
	}
	
	/**
	 * Can we tell when a key is invalid because no record exists?
	 */
	@Test
	public void testIsValidKey02() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		boolean result = objectUnderTest.isValidKey(key);
		assertFalse("Should be invalid because there are no records", result);
	}
	
	/**
	 * Can we tell when a key is invalid because it's marked as disabled?
	 */
	@Test
	public void testIsValidKey03() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		objectUnderTest.storeNewKey("test@example.com", key, SignupMethod.EMAIL);
		objectUnderTest.disableKey(key);
		boolean result = objectUnderTest.isValidKey(key);
		assertFalse("Should be invalid because the key is disabled", result);
	}
	
	// TODO string comparison disableKey test
	
	/**
	 * Can we enable a disabled key?
	 */
	@Test
	public void testEnableKey01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		objectUnderTest.storeNewKey("test@example.com", key, SignupMethod.EMAIL);
		objectUnderTest.disableKey(key);
		boolean result1 = objectUnderTest.isValidKey(key);
		assertFalse("Should be invalid because the key is disabled", result1);
		objectUnderTest.enableKey(key);
		boolean result2 = objectUnderTest.isValidKey(key);
		assertTrue("Should be valid again because we enabled it", result2);
	}
}
