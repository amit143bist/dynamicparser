package com.docusign.report.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FailureCode {

	ERROR_101("InterruptedException"),
	ERROR_102("ExecutionException"),
	ERROR_103("NoSuchMethodException"),
	ERROR_104("ScriptException"),
	ERROR_105("SQLException"),
	ERROR_106("HttpClientErrorException"),
	ERROR_107("UnknownException"),
	ERROR_108("UserDoesNotExistException"),
	ERROR_109("IOException");

	@Getter
	private String failureCodeDescription;
}