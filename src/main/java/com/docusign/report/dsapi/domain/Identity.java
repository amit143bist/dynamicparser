
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
@JsonPropertyOrder({ "id", "provider_id", "user_id", "immutable_id" })
public class Identity {

	@JsonProperty("id")
	private String id;
	@JsonProperty("provider_id")
	private String providerId;
	@JsonProperty("user_id")
	private String userId;
	@JsonProperty("immutable_id")
	private String immutableId;

}