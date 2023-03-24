package com.docusign.report.db.domain;

import java.util.List;

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
@JsonPropertyOrder({ "totalBatchesCount", "scheduledBatchLogResponses" })
public class ScheduledBatchLogsInformation implements ReportInformation {

	@JsonProperty("totalBatchesCount")
	private Long totalBatchesCount;
	@JsonProperty("scheduledBatchLogResponses")
	private List<ScheduledBatchLogResponse> scheduledBatchLogResponses = null;

}