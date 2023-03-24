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
@JsonPropertyOrder({ "userName", "userId", "userType", "isAdmin", "isNAREnabled", "userStatus", "uri", "email",
		"createdDateTime", "userAddedToAccountDateTime", "firstName", "lastName", "jobTitle", "company",
		"permissionProfileId", "permissionProfileName", "userSettings", "sendActivationOnInvalidLogin",
		"enableConnectForUser", "groupList", "workAddress", "homeAddress", "lastLogin", "loginStatus",
		"userProfileLastModifiedDate", "signatureImageUri", "initialsImageUri", "defaultAccountId", "customSettings" })
public class AccountUser {

	@JsonProperty("userName")
	private String userName;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("userType")
	private String userType;
	@JsonProperty("isAdmin")
	private String isAdmin;
	@JsonProperty("isNAREnabled")
	private String isNAREnabled;
	@JsonProperty("userStatus")
	private String userStatus;
	@JsonProperty("uri")
	private String uri;
	@JsonProperty("email")
	private String email;
	@JsonProperty("createdDateTime")
	private String createdDateTime;
	@JsonProperty("userAddedToAccountDateTime")
	private String userAddedToAccountDateTime;
	@JsonProperty("firstName")
	private String firstName;
	@JsonProperty("lastName")
	private String lastName;
	@JsonProperty("jobTitle")
	private String jobTitle;
	@JsonProperty("company")
	private String company;
	@JsonProperty("permissionProfileId")
	private String permissionProfileId;
	@JsonProperty("permissionProfileName")
	private String permissionProfileName;
	@JsonProperty("userSettings")
	private AccountUserSettings userSettings;
	@JsonProperty("sendActivationOnInvalidLogin")
	private String sendActivationOnInvalidLogin;
	@JsonProperty("enableConnectForUser")
	private String enableConnectForUser;
	@JsonProperty("groupList")
	private List<GroupList> groupList = null;
	@JsonProperty("workAddress")
	private Address workAddress;
	@JsonProperty("homeAddress")
	private Address homeAddress;
	@JsonProperty("lastLogin")
	private String lastLogin;
	@JsonProperty("loginStatus")
	private String loginStatus;
	@JsonProperty("userProfileLastModifiedDate")
	private String userProfileLastModifiedDate;
	@JsonProperty("signatureImageUri")
	private String signatureImageUri;
	@JsonProperty("initialsImageUri")
	private String initialsImageUri;
	@JsonProperty("defaultAccountId")
	private String defaultAccountId;
	@JsonProperty("customSettings")
	private List<CustomSetting> customSettings = null;
}