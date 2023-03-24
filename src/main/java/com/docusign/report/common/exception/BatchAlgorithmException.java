package com.docusign.report.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BatchAlgorithmException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3300656252810786755L;

	public BatchAlgorithmException(String message) {
		super(message);
	}

	public BatchAlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}
}