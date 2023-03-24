package com.docusign.report.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConsentRequiredException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3434712540996918191L;

	public ConsentRequiredException(String message) {
		super(message);
	}

	public ConsentRequiredException(String message, Throwable cause) {
		super(message, cause);
	}
}