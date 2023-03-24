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
@JsonPropertyOrder({ "sub", "name", "given_name", "family_name", "created", "email", "accounts" })
public class UserInformation {

	@JsonProperty("sub")
	private String sub;
	@JsonProperty("name")
	private String name;
	@JsonProperty("given_name")
	private String givenName;
	@JsonProperty("family_name")
	private String familyName;
	@JsonProperty("created")
	private String created;
	@JsonProperty("email")
	private String email;
	@JsonProperty("accounts")
	private List<UserAccount> userAccounts = null;

}