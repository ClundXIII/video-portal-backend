package co.clund.exception;

public class RateLimitException extends Exception {

	private static final long serialVersionUID = 5217966351908330333L;

	public RateLimitException() {
	}

	public RateLimitException(String message) {
		super(message);
	}

	public RateLimitException(Throwable cause) {
		super(cause);
	}

	public RateLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	public RateLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
