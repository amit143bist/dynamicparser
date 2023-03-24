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
@JsonPropertyOrder({ "columnName", "columnType", "csvHeaderName" })
public class Column {

	@JsonProperty("columnName")
	private String columnName;
	@JsonProperty("columnType")
	private String columnType;
	@JsonProperty("csvHeaderName")
	private String csvHeaderName;

}