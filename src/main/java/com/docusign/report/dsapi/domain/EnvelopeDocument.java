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
@JsonPropertyOrder({ "documentId", "documentIdGuid", "name", "type", "uri", "order", "pages", "availableDocumentTypes",
		"display", "includeInDownload", "signerMustAcknowledge", "templateRequired", "authoritativeCopy",
		"attachmentTabId" })
public class EnvelopeDocument {

	@JsonProperty("documentId")
	private String documentId;
	@JsonProperty("documentIdGuid")
	private String documentIdGuid;
	@JsonProperty("name")
	private String name;
	@JsonProperty("type")
	private String type;
	@JsonProperty("uri")
	private String uri;
	@JsonProperty("order")
	private String order;
	@JsonProperty("pages")
	private List<Page> pages = null;
	@JsonProperty("availableDocumentTypes")
	private List<AvailableDocumentType> availableDocumentTypes = null;
	@JsonProperty("display")
	private String display;
	@JsonProperty("includeInDownload")
	private String includeInDownload;
	@JsonProperty("signerMustAcknowledge")
	private String signerMustAcknowledge;
	@JsonProperty("templateRequired")
	private String templateRequired;
	@JsonProperty("authoritativeCopy")
	private String authoritativeCopy;
	@JsonProperty("attachmentTabId")
	private String attachmentTabId;

}