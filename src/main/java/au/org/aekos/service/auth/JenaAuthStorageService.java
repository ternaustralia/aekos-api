package au.org.aekos.service.auth;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JenaAuthStorageService implements AuthStorageService {

	private static final String AUTH_NAMESPACE = "http://www.aekos.org.au/api/1.0/auth#";
	private static final String OWNED_BY_PROP = AUTH_NAMESPACE + "ownedBy";
	private static final String SIGNUP_METHOD_PROP = AUTH_NAMESPACE + "signupMethod";
	private static final String DISABLED_PROP = AUTH_NAMESPACE + "disabled";
	
	@Autowired
	@Qualifier("authModel")
	private Model authModel;
	
	@Override
	public void storeNewKey(String emailAddress, AekosApiAuthKey key, SignupMethod signupMethod) {
		Property ownedByProp = authModel.createProperty(OWNED_BY_PROP);
		Property signupMethodProp = authModel.createProperty(SIGNUP_METHOD_PROP);
		Resource keyEntityType = authModel.createResource(AUTH_NAMESPACE + key.getClass().getSimpleName());
		Resource subject = authModel.createResource(AUTH_NAMESPACE + key.getKeyStringValue(), keyEntityType);
		subject.addLiteral(ownedByProp, emailAddress);
		subject.addLiteral(signupMethodProp, signupMethod.toString());
	}

	@Override
	public boolean isValidKey(AekosApiAuthKey key) {
		Resource subject = authModel.createResource(AUTH_NAMESPACE + key.getKeyStringValue());
		boolean resourceDoesNotExist = subject.listProperties().toList().size() == 0;
		if (resourceDoesNotExist) {
			return false;
		}
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		boolean keyIsMarkedAsDisabled = keyDisabledStatement != null && keyDisabledStatement.getBoolean() == true;
		if (keyIsMarkedAsDisabled) {
			return false;
		}
		return true;
	}
	
	@Override
	public void disableKey(AekosApiAuthKey key) {
		// TODO check if key exists
		Resource subject = authModel.createResource(AUTH_NAMESPACE + key.getKeyStringValue());
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		if (keyDisabledStatement != null) {
			keyDisabledStatement.remove();
		}
		subject.addLiteral(disabledProp, true);
	}

	@Override
	public void enableKey(AekosApiAuthKey key) {
		// TODO check if key exists
		Resource subject = authModel.createResource(AUTH_NAMESPACE + key.getKeyStringValue());
		Property disabledProp = authModel.createProperty(DISABLED_PROP);
		Statement keyDisabledStatement = subject.getProperty(disabledProp);
		if (keyDisabledStatement == null) {
			return;
		}
		keyDisabledStatement.remove();
	}

	public void setAuthModel(Model authModel) {
		this.authModel = authModel;
	}
}