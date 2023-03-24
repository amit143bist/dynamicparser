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
@JsonPropertyOrder({ "groupId", "groupName", "groupType", "permissionProfileId" })
public class GroupList {

	@JsonProperty("groupId")
	private String groupId;
	@JsonProperty("groupName")
	private String groupName;
	@JsonProperty("groupType")
	private String groupType;
	@JsonProperty("permissionProfileId")
	private String permissionProfileId;

}