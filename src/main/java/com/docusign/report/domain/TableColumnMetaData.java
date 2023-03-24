package com.docusign.report.domain;

import java.util.Map;

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
@JsonPropertyOrder({ "tableName", "insertQuery", "insertNamedQuery", "columnNameTypeMap", "columnNameIndexMap",
		"columnNameHeaderMap" })
public class TableColumnMetaData {

	@JsonProperty("tableName")
	private String tableName;

	@JsonProperty("insertQuery")
	private String insertQuery;

	@JsonProperty("insertNamedQuery")
	private String insertNamedQuery;

	@JsonProperty("columnNameTypeMap")
	private Map<String, String> columnNameTypeMap;

	@JsonProperty("columnNameIndexMap")
	private Map<String, Integer> columnNameIndexMap;

}