package com.basdado.trainfinder.exception;

public class TravelAdviceException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TravelAdviceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public TravelAdviceException(String message) {
		super(message);
	}
	
	public TravelAdviceException(Throwable cause) {
		super(cause);
	}
}
