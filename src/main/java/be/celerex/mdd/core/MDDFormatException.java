package be.celerex.mdd.core;

public class MDDFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public MDDFormatException() {
		super();
	}

	public MDDFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MDDFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public MDDFormatException(String message) {
		super(message);
	}

	public MDDFormatException(Throwable cause) {
		super(cause);
	}
	

}
