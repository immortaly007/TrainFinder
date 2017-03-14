package com.basdado.trainfinder.exception;

public class PathFindingException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public PathFindingException() {}
	public PathFindingException(String message) {
		super(message);
	}
	public PathFindingException(String message, Throwable cause) {
		super(message, cause);
	}
	public PathFindingException(Throwable cause) {
		super(cause);
	}
}
