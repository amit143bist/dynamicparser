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
@JsonPropertyOrder({ "pageId", "sequence", "height", "width", "dpi" })
public class Page {

	@JsonProperty("pageId")
	private String pageId;
	@JsonProperty("sequence")
	private String sequence;
	@JsonProperty("height")
	private String height;
	@JsonProperty("width")
	private String width;
	@JsonProperty("dpi")
	private String dpi;

}