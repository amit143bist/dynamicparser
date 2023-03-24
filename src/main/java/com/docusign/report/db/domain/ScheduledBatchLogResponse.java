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
@JsonPropertyOrder({ "batchId", "batchType", "batchStartDateTime", "batchEndDateTime", "batchStartParameters",
		"totalRecords" })
public class ScheduledBatchLogResponse implements ReportInformation {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("batchType")
	private String batchType;
	@JsonProperty("batchStartDateTime")
	private Long batchStartDateTime;
	@JsonProperty("batchEndDateTime")
	private Long batchEndDateTime;
	@JsonProperty("batchStartParameters")
	private String batchStartParameters;
	@JsonProperty("totalRecords")
	private Long totalRecords;

}