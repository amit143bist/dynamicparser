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
@JsonPropertyOrder({ "canManageUsers", "canManageAdmins", "canManageSharing", "canManageAccountSettings",
		"canManageReporting", "canManageAccountSecuritySettings", "canManageSigningGroups" })
public class AccountManagementGranular {

	@JsonProperty("canManageUsers")
	private String canManageUsers;
	@JsonProperty("canManageAdmins")
	private String canManageAdmins;
	@JsonProperty("canManageSharing")
	private String canManageSharing;
	@JsonProperty("canManageAccountSettings")
	private String canManageAccountSettings;
	@JsonProperty("canManageReporting")
	private String canManageReporting;
	@JsonProperty("canManageAccountSecuritySettings")
	private String canManageAccountSecuritySettings;
	@JsonProperty("canManageSigningGroups")
	private String canManageSigningGroups;

}