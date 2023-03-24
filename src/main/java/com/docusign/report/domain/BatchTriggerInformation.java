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
@JsonPropertyOrder({ "batchStartDateTime", "batchEndDateTime", "numberOfHours", "jobType", "pathParams" })
public class BatchTriggerInformation implements ReportInformation {

	@JsonProperty("batchStartDateTime")
	private String batchStartDateTime;
	@JsonProperty("batchEndDateTime")
	private String batchEndDateTime;
	@JsonProperty("numberOfHours")
	private Integer numberOfHours;
	@JsonProperty("jobType")
	private String jobType;
	@JsonProperty("pathParams")
	private List<PathParam> pathParams;
}