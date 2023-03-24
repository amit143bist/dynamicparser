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
@JsonPropertyOrder({ "paramName", "paramValue", "paramDataType", "paramPattern" })
public class PathParam {

	@JsonProperty("paramName")
	private String paramName;
	@JsonProperty("paramValue")
	private String paramValue;
	@JsonProperty("paramDataType")
	private String paramDataType;
	@JsonProperty("paramPattern")
	private String paramPattern;

}