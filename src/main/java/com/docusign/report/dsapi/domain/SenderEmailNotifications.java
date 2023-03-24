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
@JsonPropertyOrder({ "envelopeComplete", "changedSigner", "senderEnvelopeDeclined", "withdrawnConsent",
		"recipientViewed", "deliveryFailed", "offlineSigningFailed", "purgeDocuments", "commentsReceiveAll",
		"commentsOnlyPrivateAndMention" })
public class SenderEmailNotifications {

	@JsonProperty("envelopeComplete")
	private String envelopeComplete;
	@JsonProperty("changedSigner")
	private String changedSigner;
	@JsonProperty("senderEnvelopeDeclined")
	private String senderEnvelopeDeclined;
	@JsonProperty("withdrawnConsent")
	private String withdrawnConsent;
	@JsonProperty("recipientViewed")
	private String recipientViewed;
	@JsonProperty("deliveryFailed")
	private String deliveryFailed;
	@JsonProperty("offlineSigningFailed")
	private String offlineSigningFailed;
	@JsonProperty("purgeDocuments")
	private String purgeDocuments;
	@JsonProperty("commentsReceiveAll")
	private String commentsReceiveAll;
	@JsonProperty("commentsOnlyPrivateAndMention")
	private String commentsOnlyPrivateAndMention;

}