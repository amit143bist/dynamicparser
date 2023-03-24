package com.docusign.report.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
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
@JsonPropertyOrder({ "tableName", "selectSql", "csvColumns", "whereClause", "orderByClause", "groupByColumn",
		"sqlParams", "decorateOutput", "exportRunArgs", "downloadDocs" })
public class ManageDataAPI {

	@JsonProperty("tableName")
	private String tableName;
	@JsonProperty("selectSql")
	private String selectSql;
	@JsonProperty("csvColumns")
	private String csvColumns;
	@JsonProperty("whereClause")
	private String whereClause;
	@JsonProperty("orderByClause")
	private String orderByClause;
	@JsonProperty("groupByColumn")
	private String groupByColumn;
	@JsonProperty("sqlParams")
	private List<PathParam> sqlParams = null;
	@JsonProperty("decorateOutput")
	private List<DecorateOutput> decorateOutput = null;
	@JsonProperty(value = "exportRunArgs")
	private ReportRunArgs exportRunArgs;
	@JsonProperty("downloadDocs")
	private DownloadDocs downloadDocs;// Used for CSV Generate

	@JsonCreator
	public ManageDataAPI(@JsonProperty(value = "tableName", required = true) String tableName,
			@JsonProperty(value = "selectSql", required = true) String selectSql) {
		this.tableName = tableName;
		this.selectSql = selectSql;
	}
}