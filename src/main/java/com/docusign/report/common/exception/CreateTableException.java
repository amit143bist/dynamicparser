package com.docusign.report.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class CreateTableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3434712540996918191L;

	public CreateTableException(String message) {
		super(message);
	}

	public CreateTableException(String message, Throwable cause) {
		super(message, cause);
	}
}