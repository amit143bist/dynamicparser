
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
@JsonPropertyOrder({ "id", "name", "default_account_id", "default_permission_profile_id", "created_on", "created_by",
		"last_modified_on", "last_modified_by", "accounts", "users", "reserved_domains", "identity_providers" })
public class Organization {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("default_account_id")
	private String defaultAccountId;
	@JsonProperty("default_permission_profile_id")
	private Integer defaultPermissionProfileId;
	@JsonProperty("created_on")
	private String createdOn;
	@JsonProperty("created_by")
	private String createdBy;
	@JsonProperty("last_modified_on")
	private String lastModifiedOn;
	@JsonProperty("last_modified_by")
	private String lastModifiedBy;
	@JsonProperty("accounts")
	private List<Account> accounts = null;
	@JsonProperty("users")
	private List<User> users = null;
	@JsonProperty("reserved_domains")
	private List<ReservedDomain> reservedDomains = null;
	@JsonProperty("identity_providers")
	private List<IdentityProvider> identityProviders = null;

}