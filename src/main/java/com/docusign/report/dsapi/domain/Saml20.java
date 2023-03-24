
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
@JsonPropertyOrder({ "issuer", "settings", "certificates", "attribute_mappings", "has_valid_certificate" })
public class Saml20 {

	@JsonProperty("issuer")
	private String issuer;
	@JsonProperty("settings")
	private List<Setting> settings = null;
	@JsonProperty("certificates")
	private List<Certificate> certificates = null;
	@JsonProperty("attribute_mappings")
	private List<Object> attributeMappings = null;
	@JsonProperty("has_valid_certificate")
	private Boolean hasValidCertificate;

}