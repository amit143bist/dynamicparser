package com.docusign.report.db.domain;

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
@JsonPropertyOrder({ "cacheId", "cacheKey", "cacheValue", "cachereference", "processFlag" })
public class CacheLogInformation {

	@JsonProperty("cacheId")
	private String cacheId;
	@JsonProperty("cacheKey")
	private String cacheKey;
	@JsonProperty("cacheValue")
	private String cacheValue;
	@JsonProperty("cachereference")
	private String cacheReference;
	@JsonProperty("processFlag")
	private String processFlag;

}