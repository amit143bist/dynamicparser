
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
@JsonPropertyOrder({ "email", "account_id", "external_account_id", "account_name", "is_external_account", "status",
		"permission_profile", "created_on", "groups", "is_admin" })
public class Membership {

	@JsonProperty("email")
	private String email;
	@JsonProperty("account_id")
	private String accountId;
	@JsonProperty("external_account_id")
	private String externalAccountId;
	@JsonProperty("account_name")
	private String accountName;
	@JsonProperty("is_external_account")
	private Boolean isExternalAccount;
	@JsonProperty("status")
	private String status;
	@JsonProperty("permission_profile")
	private PermissionProfile permissionProfile;
	@JsonProperty("created_on")
	private String createdOn;
	@JsonProperty("groups")
	private List<Group> groups = null;
	@JsonProperty("is_admin")
	private Boolean isAdmin;

}