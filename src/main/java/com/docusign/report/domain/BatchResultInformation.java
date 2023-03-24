package com.docusign.report.domain;

import java.util.List;

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
@JsonPropertyOrder({ "batchId", "batchStatus", "successAccountIds", "failedAccountIds" })
public class BatchResultInformation {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("batchStatus")
	private String batchStatus;
	@JsonProperty("successAccountIds")
	private List<String> successAccountIds;
	@JsonProperty("failedAccountIds")
	private List<String> failedAccountIds;
}