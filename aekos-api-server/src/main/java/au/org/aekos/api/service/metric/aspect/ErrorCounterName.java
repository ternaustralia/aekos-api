package au.org.aekos.api.service.metric.aspect;

class ErrorCounterName {
	private final String prefix;
	private final String controllerName;
	private final String methodName;
	
	public ErrorCounterName(String controllerName, String methodName) {
		this("counter.errors.", controllerName, methodName);
	}
	
	protected ErrorCounterName(String prefix, String controllerName, String methodName) {
		this.prefix = prefix;
		this.controllerName = controllerName;
		this.methodName = methodName;
	}
	
	public String getFullName() {
		return prefix + controllerName + "." + methodName;
	}

	String getMethodName() {
		return methodName;
	}
}