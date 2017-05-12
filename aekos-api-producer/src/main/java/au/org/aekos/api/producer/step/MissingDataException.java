package au.org.aekos.api.producer.step;

public class MissingDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingDataException(String message) {
		super(message);
	}

	public MissingDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
