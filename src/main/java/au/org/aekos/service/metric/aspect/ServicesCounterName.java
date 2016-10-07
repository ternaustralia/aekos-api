package au.org.aekos.service.metric.aspect;

class ServicesCounterName extends ErrorCounterName {
	public ServicesCounterName(String controllerName, String methodName) {
		super("services.api.", controllerName, methodName);
	}

	@Override
	public String getFullName() {
		String superResult = super.getFullName();
		return superResult + ".invoked";
	}
}