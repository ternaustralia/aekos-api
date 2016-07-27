package au.org.aekos.service.auth;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import au.org.aekos.Application;

/**
 * Implementation that uses Apache Jena as the storage mechanism.
 * Depending on which model is wired in, this could be an in-memory model or a
 * TDB backed one.
 */
@Service
public class JenaAuthStorageService implements AuthStorageService {

	private static final String AUTH_NAMESPACE_V1_0 = Application.API_NAMESPACE_V1_0 + "auth#";
	private static final String OWNED_BY_PROP = AUTH_NAMESPACE_V1_0 + "ownedBy";
	private static final String SIGNUP_METHOD_PROP = AUTH_NAMESPACE_V1_0 + "signupMethod";
	private static final String DISABLED_PROP = AUTH_NAMESPACE_V1_0 + "disabled";
	
	@Autowired
	@Qualifier("authModel")
	private Model authModel;
	
	@Override
	public void storeNewKey(String emailAddress, AekosApiAuthKey key, SignupMethod signupMethod) {
		startTransaction();
		Property ownedByProp = authModel.createProperty(OWNED_BY_PROP);
		Property signupMethodProp = authModel.createProperty(SIGNUP_METHOD_PROP);
		Resource keyEntityType = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getClass().getSimpleName());
		Resource subject = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getKeyStringValue(), keyEntityType);
		subject.addLiteral(ownedByProp, emailAddress);
		subject.addLiteral(signupMethodProp, signupMethod.toString());
		endTransaction();
	}

	@Override
	public boolean isValidKey(AekosApiAuthKey key) {
		Resource subject = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getKeyStringValue());
		boolean resourceDoesNotExist = subject.listProperties().toList().size() == 0;
		if (resourceDoesNotExist) {
			return false;
		}
		return !isKeyDisabled(subject);
	}

	private boolean isKeyDisabled(Resource subject) {
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		boolean keyIsMarkedAsDisabled = keyDisabledStatement != null && keyDisabledStatement.getBoolean() == true;
		if (keyIsMarkedAsDisabled) {
			return true;
		}
		return false;
	}
	
	@Override
	public void disableKey(AekosApiAuthKey key) {
		if (!existsPrivate(key)) {
			return;
		}
		startTransaction();
		Resource subject = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getKeyStringValue());
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		if (keyDisabledStatement != null) {
			keyDisabledStatement.remove();
		}
		subject.addLiteral(disabledProp, true);
		endTransaction();
	}

	@Override
	public void enableKey(AekosApiAuthKey key) {
		startTransaction();
		Resource subject = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getKeyStringValue());
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		if (keyDisabledStatement == null) {
			return;
		}
		keyDisabledStatement.remove();
		endTransaction();
	}
	
	@Override
	public boolean exists(AekosApiAuthKey key) {
		return existsPrivate(key);
	}
	
	@Override
	public KeySummary getSummary() {
		int enabledCount = 0;
		int disabledCount = 0;
		for (ResIterator it = authModel.listSubjects(); it.hasNext();) {
			Resource currKey = it.next();
			if (isKeyDisabled(currKey)) {
				disabledCount++;
				continue;
			}
			enabledCount++;
		}
		return new KeySummary(enabledCount, disabledCount);
	}

	private boolean existsPrivate(AekosApiAuthKey key) {
		Resource subject = authModel.createResource(AUTH_NAMESPACE_V1_0 + key.getKeyStringValue());
		if (authModel.containsResource(subject)) {
			return true;
		}
		return false;
	}
	
	private void startTransaction() {
		if (!authModel.supportsTransactions()) {
			return;
		}
		authModel.begin();
	}
	
	private void endTransaction() {
		if (!authModel.supportsTransactions()) {
			return;
		}
		authModel.commit();
	}

	public void setAuthModel(Model authModel) {
		this.authModel = authModel;
	}
}
