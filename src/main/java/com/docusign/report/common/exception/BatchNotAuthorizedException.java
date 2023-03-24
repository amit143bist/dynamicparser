package com.docusign.report.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BatchNotAuthorizedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2640871767756130498L;

	public BatchNotAuthorizedException(String message) {
		super(message);
	}

	public BatchNotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}