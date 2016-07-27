package au.org.aekos.service.auth;

import static org.hamcrest.CoreMatchers.is;
import static au.org.aekos.TestUtils.loadAuth;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;

import au.org.aekos.service.auth.AekosApiAuthKey.InvalidKeyException;
import au.org.aekos.service.auth.AuthStorageService.KeySummary;
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
		assertThat(out.toString(), is(loadAuth("testStoreNewKey01_expected.ttl")));
	}
	
	/**
	 * Can we store a key using a TDB instance and transactions?
	 */
	@Test
	public void testStoreNewKey02() throws Throwable {
		Path tempDir = Files.createTempDirectory("testStoreNewKey02");
		tempDir.toFile().deleteOnExit();
		Model authModel = TDBFactory.createDataset(tempDir.toString()).getDefaultModel();
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		objectUnderTest.setAuthModel(authModel);
		objectUnderTest.storeNewKey("test@example.com", new AekosApiAuthKey("AAABBB123"), SignupMethod.EMAIL);
		assertThat(objectUnderTest.getSummary().getTotalCount(), is(1));
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
	
	/**
	 * Does enabling a key that doesn't exist have no effect (especially not creating the key in the store)?
	 */
	@Test
	public void testEnableKey02() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		assertTrue("There should be no keys in the store", authModel.isEmpty());
		objectUnderTest.enableKey(key);
		assertTrue("There should STILL be no keys in the store", authModel.isEmpty());
	}
	
	/**
	 * Does disabling a key that doesn't exist have no effect (especially not creating the key in the store)?
	 */
	@Test
	public void testDisableKey01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		assertTrue("There should be no keys in the store", authModel.isEmpty());
		objectUnderTest.disableKey(key);
		assertTrue("There should STILL be no keys in the store", authModel.isEmpty());
	}
	
	/**
	 * Does disabling a key write what we expect to the data store?
	 */
	@Test
	public void testDisableKey02() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		objectUnderTest.storeNewKey("test@example.com", key, SignupMethod.GOOGLE);
		objectUnderTest.disableKey(key);
		Writer out = new StringWriter();
		authModel.write(out, "TURTLE");
		assertThat(out.toString(), is(loadAuth("testDisableKey02_expected.ttl")));
	}
	
	/**
	 * Can we tell when a key exists?
	 */
	@Test
	public void testExists01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		objectUnderTest.storeNewKey("test@example.com", key, SignupMethod.EMAIL);
		boolean result = objectUnderTest.exists(key);
		assertTrue("Should exist because we just created it", result);
	}
	
	/**
	 * Can we tell when a key doesn't exist?
	 */
	@Test
	public void testExists02() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		AekosApiAuthKey key = new AekosApiAuthKey("AAABBB123");
		assertTrue(authModel.isEmpty());
		boolean result = objectUnderTest.exists(key);
		assertFalse("Should NOT exist because we never stored it", result);
	}
	
	/**
	 * Can we get a key summary when there is data?
	 */
	@Test
	public void testSummary01() throws InvalidKeyException {
		JenaAuthStorageService objectUnderTest = new JenaAuthStorageService();
		Model authModel = ModelFactory.createDefaultModel();
		objectUnderTest.setAuthModel(authModel);
		objectUnderTest.storeNewKey("test@example.com", new AekosApiAuthKey("AAABBB123"), SignupMethod.EMAIL);
		objectUnderTest.storeNewKey("test2@example.com", new AekosApiAuthKey("CCCDDD456"), SignupMethod.EMAIL);
		AekosApiAuthKey key3 = new AekosApiAuthKey("EEEFFF789");
		objectUnderTest.storeNewKey("test3@example.com", key3, SignupMethod.EMAIL);
		objectUnderTest.disableKey(key3);
		KeySummary result = objectUnderTest.getSummary();
		assertThat(result.getTotalCount(), is(3));
		assertThat(result.getEnabledCount(), is(2));
		assertThat(result.getDisabledCount(), is(1));
	}
}
