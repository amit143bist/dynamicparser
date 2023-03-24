
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
@JsonPropertyOrder({ "id", "site_id", "site_name", "user_name", "first_name", "last_name", "user_status",
		"default_account_id", "default_account_name", "language_culture", "federated_status", "is_organization_admin",
		"created_on", "memberships", "identities" })
public class User {

	@JsonProperty("id")
	private String id;
	@JsonProperty("site_id")
	private Integer siteId;
	@JsonProperty("site_name")
	private String siteName;
	@JsonProperty("user_name")
	private String userName;
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastName;
	@JsonProperty("user_status")
	private String userStatus;
	@JsonProperty("default_account_id")
	private String defaultAccountId;
	@JsonProperty("default_account_name")
	private String defaultAccountName;
	@JsonProperty("language_culture")
	private String languageCulture;
	@JsonProperty("federated_status")
	private String federatedStatus;
	@JsonProperty("is_organization_admin")
	private Boolean isOrganizationAdmin;
	@JsonProperty("created_on")
	private String createdOn;
	@JsonProperty("memberships")
	private List<Membership> memberships = null;
	@JsonProperty("identities")
	private List<Identity> identities = null;

}