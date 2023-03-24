package com.docusign.report.dsapi.domain;

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
@JsonPropertyOrder({ "account_id", "is_default", "account_name", "base_uri", "organization" })
public class UserAccount {

	@JsonProperty("account_id")
	private String accountId;
	@JsonProperty("is_default")
	private Boolean isDefault;
	@JsonProperty("account_name")
	private String accountName;
	@JsonProperty("base_uri")
	private String baseUri;
	@JsonProperty("organization")
	private UserOrganization userOrganization;

}