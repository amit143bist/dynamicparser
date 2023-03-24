
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
@JsonPropertyOrder({ "id", "status", "host_name", "txt_token", "settings" })
public class ReservedDomain {

	@JsonProperty("id")
	private String id;
	@JsonProperty("status")
	private String status;
	@JsonProperty("host_name")
	private String hostName;
	@JsonProperty("txt_token")
	private String txtToken;
	@JsonProperty("settings")
	private List<Setting> settings = null;

}