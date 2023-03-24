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
@JsonPropertyOrder({ "apiId", "apiUri", "apiNextPaginationPath", "apiDataTableName", "apiTotalSetSizePath",
		"apiCategory", "apiOperationType", "apiContentType", "apiAcceptType", "saveDataInCache", "apiParams",
		"commonFilters", "outputColumns", "filterAccountIds", "apiRunArgs", "outputApiUri", "outputApiCategory",
		"outputApiOperationType", "outputApiPrimaryId", "outputApiReqProperty" })
public class PrepareDataAPI {

	@JsonProperty("apiId")
	private Integer apiId;
	@JsonProperty("apiUri")
	private String apiUri;
	@JsonProperty("apiNextPaginationPath")
	private String apiNextPaginationPath;
	@JsonProperty("apiDataTableName")
	private String apiDataTableName;
	@JsonProperty("apiTotalSetSizePath")
	private String apiTotalSetSizePath;
	@JsonProperty("apiCategory")
	private String apiCategory;
	@JsonProperty("apiOperationType")
	private String apiOperationType;
	@JsonProperty("apiContentType")
	private String apiContentType;
	@JsonProperty("apiAcceptType")
	private String apiAcceptType;
	@JsonProperty("saveDataInCache")
	private String saveDataInCache;
	@JsonProperty("apiParams")
	private List<PathParam> apiParams = null;
	@JsonProperty("commonFilters")
	private List<Filter> commonFilters = null;
	@JsonProperty("outputColumns")
	private List<OutputColumn> outputColumns = null;
	@JsonProperty("filterAccountIds")
	private String filterAccountIds;
	@JsonProperty(value = "apiRunArgs")
	private ReportRunArgs apiRunArgs;

	// Below properties added to update calling app with report results
	@JsonProperty("outputApiUri")
	private String outputApiUri;
	@JsonProperty("outputApiCategory")
	private String outputApiCategory;
	@JsonProperty("outputApiOperationType")
	private String outputApiOperationType;
	@JsonProperty("outputApiPrimaryId")
	private String outputApiPrimaryId;
	@JsonProperty("outputApiReqProperty")
	private String outputApiReqProperty;

	@JsonCreator
	public PrepareDataAPI(@JsonProperty(value = "apiUri", required = true) String apiUri,
			@JsonProperty(value = "apiCategory", required = true) String apiCategory,
			@JsonProperty(value = "apiOperationType", required = true) String apiOperationType,
			@JsonProperty(value = "apiId", required = true) Integer apiId) {

		this.apiUri = apiUri;
		this.apiCategory = apiCategory;
		this.apiOperationType = apiOperationType;
	}

}