package cn.tedu.store.service.ex;

public class DataNotFoundException 
	extends ServiceException {

	private static final long serialVersionUID = 3029101533055289190L;

	public DataNotFoundException() {
		super();
	}

	public DataNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataNotFoundException(String message) {
		super(message);
	}

	public DataNotFoundException(Throwable cause) {
		super(cause);
	}

}
