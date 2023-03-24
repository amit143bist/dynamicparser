package com.docusign.report.authentication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "accountGuid", "userId", "scopes", "apiCategory", "clientId", "clientSecret", "userName",
		"password", "baseUrl", "apiId" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

	@JsonProperty("accountGuid")
	private String accountGuid;

	@JsonProperty("scopes")
	private String scopes;

	@JsonProperty("userId")
	private String userId;

	@JsonProperty("apiCategory")
	private String apiCategory;

	@JsonProperty("clientId")
	private String clientId;

	@JsonProperty("clientSecret")
	private String clientSecret;

	@JsonProperty("userName")
	private String userName;

	@JsonProperty("password")
	private String password;

	@JsonProperty("baseUrl")
	private String baseUrl;

	@JsonProperty("apiId")
	private Integer apiId;

}