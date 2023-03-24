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
@JsonPropertyOrder({ "outputPatternExpression", "dbColumnName", "outputType", "paramValue", "inputDatePattern",
		"outputDatePattern", "outputDateZone", "outputDelimiter", "keyValueDelimiter", "pathParams" })
public class DecorateOutput {

	@JsonProperty("outputPatternExpression")
	private String outputPatternExpression;
	@JsonProperty("dbColumnName")
	public String dbColumnName;
	@JsonProperty("outputType")
	public String outputType;
	@JsonProperty("paramValue")
	public String paramValue;
	@JsonProperty("inputDatePattern")
	public String inputDatePattern;
	@JsonProperty("outputDatePattern")
	public String outputDatePattern;
	@JsonProperty("outputDateZone")
	public String outputDateZone;
	@JsonProperty("outputDelimiter")
	public String outputDelimiter;
	@JsonProperty("keyValueDelimiter")
	public String keyValueDelimiter;
	@JsonProperty("pathParams")
	private List<PathParam> pathParams = null;

}