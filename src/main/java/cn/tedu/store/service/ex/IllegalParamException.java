package cn.tedu.store.service.ex;

public class IllegalParamException extends ServiceException {

	private static final long serialVersionUID = 1049885624837101702L;

	public IllegalParamException() {
		super();
	}

	public IllegalParamException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalParamException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalParamException(String message) {
		super(message);
	}

	public IllegalParamException(Throwable cause) {
		super(cause);
	}

}
