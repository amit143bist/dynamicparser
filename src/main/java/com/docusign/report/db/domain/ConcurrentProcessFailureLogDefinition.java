package com.docusign.report.db.domain;

import com.docusign.report.domain.ReportInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "processFailureId", "processId", "failureCode", "failureReason", "failureDateTime",
		"successDateTime", "failureRecordId", "failureStep", "retryStatus", "retryCount" })
public class ConcurrentProcessFailureLogDefinition implements ReportInformation {

	@JsonProperty("processFailureId")
	private String processFailureId;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("failureCode")
	private String failureCode;
	@JsonProperty("failureReason")
	private String failureReason;
	@JsonProperty("failureDateTime")
	private String failureDateTime;
	@JsonProperty("successDateTime")
	private String successDateTime;
	@JsonProperty("failureRecordId")
	private String failureRecordId;
	@JsonProperty("failureStep")
	private String failureStep;
	@JsonProperty("retryStatus")
	private String retryStatus;
	@JsonProperty("retryCount")
	private Long retryCount;

}