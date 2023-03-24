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
@JsonPropertyOrder({ "columnName", "columnPath", "columnDataType", "columnTabType", "columnDataPattern",
		"columnOutputDataPattern", "timeZone", "startIndex", "endIndex", "outputDelimiter", "columnDataArrayIndex",
		"columnDataMapKey", "skipDataSave", "decorateOutput", "columnFilters", "associatedData" })
public class OutputColumn {

	@JsonProperty("columnName")
	private String columnName;
	@JsonProperty("columnDataType")
	private String columnDataType;
	@JsonProperty("columnTabType")
	private String columnTabType;
	@JsonProperty("columnDataPattern")
	private String columnDataPattern;
	@JsonProperty("columnOutputDataPattern")
	private String columnOutputDataPattern;
	@JsonProperty("timeZone")
	private String timeZone;
	@JsonProperty("startIndex")
	private Integer startIndex;
	@JsonProperty("endIndex")
	private Integer endIndex;
	@JsonProperty("outputDelimiter")
	private String outputDelimiter;
	@JsonProperty("columnPath")
	private String columnPath;
	@JsonProperty("columnDataArrayIndex")
	private Integer columnDataArrayIndex;
	@JsonProperty("columnDataMapKey")
	private String columnDataMapKey;
	@JsonProperty("decorateOutput")
	public DecorateOutput decorateOutput;
	@JsonProperty("columnFilters")
	private List<Filter> columnFilters = null;
	@JsonProperty("associatedData")
	private PrepareDataAPI associatedData;

}