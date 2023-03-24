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
@JsonPropertyOrder({ "processId", "batchId", "processStartDateTime", "processEndDateTime", "processStatus",
		"totalRecordsInProcess", "groupId", "accountId", "userId" })
public class ConcurrentProcessLogDefinition implements ReportInformation {

	@JsonProperty("processId")
	private String processId;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("processStartDateTime")
	private Long processStartDateTime;
	@JsonProperty("processEndDateTime")
	private Long processEndDateTime;
	@JsonProperty("processStatus")
	private String processStatus;
	@JsonProperty("totalRecordsInProcess")
	private Long totalRecordsInProcess;
	@JsonProperty("groupId")
	private String groupId;
	@JsonProperty("accountId")
	private String accountId;
	@JsonProperty("userId")
	private String userId;

}