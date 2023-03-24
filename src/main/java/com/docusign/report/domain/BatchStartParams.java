package com.docusign.report.domain;

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
@JsonPropertyOrder({ "beginDateTime", "endDateTime", "totalRecordIds" })
public class BatchStartParams {

	@JsonProperty("beginDateTime")
	private Long beginDateTime;
	@JsonProperty("endDateTime")
	private Long endDateTime;
	@JsonProperty("totalRecordIds")
	private Integer totalRecordIds;
}