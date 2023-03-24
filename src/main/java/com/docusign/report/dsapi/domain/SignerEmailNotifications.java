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
@JsonPropertyOrder({ "envelopeActivation", "envelopeComplete", "carbonCopyNotification",
		"certifiedDeliveryNotification", "envelopeDeclined", "envelopeVoided", "envelopeCorrected", "reassignedSigner",
		"purgeDocuments", "faxReceived", "documentMarkupActivation", "agentNotification", "offlineSigningFailed",
		"whenSigningGroupMember", "commentsReceiveAll", "commentsOnlyPrivateAndMention" })
public class SignerEmailNotifications {

	@JsonProperty("envelopeActivation")
	private String envelopeActivation;
	@JsonProperty("envelopeComplete")
	private String envelopeComplete;
	@JsonProperty("carbonCopyNotification")
	private String carbonCopyNotification;
	@JsonProperty("certifiedDeliveryNotification")
	private String certifiedDeliveryNotification;
	@JsonProperty("envelopeDeclined")
	private String envelopeDeclined;
	@JsonProperty("envelopeVoided")
	private String envelopeVoided;
	@JsonProperty("envelopeCorrected")
	private String envelopeCorrected;
	@JsonProperty("reassignedSigner")
	private String reassignedSigner;
	@JsonProperty("purgeDocuments")
	private String purgeDocuments;
	@JsonProperty("faxReceived")
	private String faxReceived;
	@JsonProperty("documentMarkupActivation")
	private String documentMarkupActivation;
	@JsonProperty("agentNotification")
	private String agentNotification;
	@JsonProperty("offlineSigningFailed")
	private String offlineSigningFailed;
	@JsonProperty("whenSigningGroupMember")
	private String whenSigningGroupMember;
	@JsonProperty("commentsReceiveAll")
	private String commentsReceiveAll;
	@JsonProperty("commentsOnlyPrivateAndMention")
	private String commentsOnlyPrivateAndMention;

}