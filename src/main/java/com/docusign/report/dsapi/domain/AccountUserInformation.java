package com.docusign.report.dsapi.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "users", "resultSetSize", "totalSetSize", "startPosition", "endPosition", "nextUri",
		"previousUri" })
public class AccountUserInformation {

	@JsonProperty("users")
	private List<AccountUser> users = null;
	@JsonProperty("resultSetSize")
	private String resultSetSize;
	@JsonProperty("totalSetSize")
	private String totalSetSize;
	@JsonProperty("startPosition")
	private String startPosition;
	@JsonProperty("endPosition")
	private String endPosition;
	@JsonProperty("nextUri")
	private String nextUri;
	@JsonProperty("previousUri")
	private String previousUri;
}