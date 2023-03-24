package com.docusign.report.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiHourlyLimitData {

	private String rateLimitReset;

	private String rateLimitLimit;

	private String rateLimitRemaining;

	private String burstLimitRemaining;

	private String burstLimitLimit;

	private String docuSignTraceToken;

	private boolean sleepThread;
}