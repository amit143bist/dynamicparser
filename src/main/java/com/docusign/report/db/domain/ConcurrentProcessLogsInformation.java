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
@JsonPropertyOrder({ "totalProcessesCount", "concurrentProcessLogDefinitions" })
public class ConcurrentProcessLogsInformation implements ReportInformation {

	@JsonProperty("totalProcessesCount")
	private Long totalProcessesCount = null;
	@JsonProperty("concurrentProcessLogDefinitions")
	private List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitions = null;

}