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
@JsonPropertyOrder({ "path", "filterId", "expression", "pathOutputDataType", "pathInputDataPattern",
		"pathOutputDataPattern", "timeZone", "startIndex", "endIndex", "outputDelimiter", "pathOutputDataArrayIndex",
		"pathOutputDataMapKey", "evaluateValue", "pathParams" })
public class Filter {

	@JsonProperty("filterId")
	private String filterId;
	@JsonProperty("expression")
	private String expression;
	@JsonProperty("path")
	private String path;
	@JsonProperty("pathOutputDataType")
	private String pathOutputDataType;
	@JsonProperty("pathInputDataPattern")
	private String pathInputDataPattern;
	@JsonProperty("pathOutputDataPattern")
	private String pathOutputDataPattern;
	@JsonProperty("timeZone")
	private String timeZone;
	@JsonProperty("startIndex")
	private Integer startIndex;
	@JsonProperty("endIndex")
	private Integer endIndex;
	@JsonProperty("outputDelimiter")
	private String outputDelimiter;
	@JsonProperty("pathOutputDataArrayIndex")
	private Integer pathOutputDataArrayIndex;
	@JsonProperty("pathOutputDataMapKey")
	private String pathOutputDataMapKey;
	@JsonProperty("evaluateValue")
	private String evaluateValue;
	@JsonProperty("pathParams")
	private List<PathParam> pathParams = null;

}