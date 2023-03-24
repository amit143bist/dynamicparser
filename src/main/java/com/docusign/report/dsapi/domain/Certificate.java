
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
@JsonPropertyOrder({ "id", "issuer", "thumbprint", "expiration_date", "is_valid", "links" })
public class Certificate {

	@JsonProperty("id")
	private String id;
	@JsonProperty("issuer")
	private String issuer;
	@JsonProperty("thumbprint")
	private String thumbprint;
	@JsonProperty("expiration_date")
	private String expirationDate;
	@JsonProperty("is_valid")
	private Boolean isValid;
	@JsonProperty("links")
	private List<Link> links = null;

}