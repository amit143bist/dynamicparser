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
@JsonPropertyOrder({ "address1", "address2", "city", "stateOrProvince", "postalCode", "phone", "country" })
public class Address {

	@JsonProperty("address1")
	private String address1;
	@JsonProperty("address2")
	private String address2;
	@JsonProperty("city")
	private String city;
	@JsonProperty("stateOrProvince")
	private String stateOrProvince;
	@JsonProperty("postalCode")
	private String postalCode;
	@JsonProperty("phone")
	private String phone;
	@JsonProperty("country")
	private String country;
}