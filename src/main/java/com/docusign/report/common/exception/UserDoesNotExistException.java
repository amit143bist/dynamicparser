package com.docusign.report.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class UserDoesNotExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2643411796429645131L;

	public UserDoesNotExistException(String message) {
		super(message);
	}

	public UserDoesNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}