
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
@JsonPropertyOrder({ "id", "friendly_name", "auto_provision_users", "type", "saml_20", "links" })
public class IdentityProvider {

	@JsonProperty("id")
	private String id;
	@JsonProperty("friendly_name")
	private String friendlyName;
	@JsonProperty("auto_provision_users")
	private Boolean autoProvisionUsers;
	@JsonProperty("type")
	private Integer type;
	@JsonProperty("saml_20")
	private Saml20 saml20;
	@JsonProperty("links")
	private List<Link> links = null;

}