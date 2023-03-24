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
@JsonPropertyOrder({ "canManageAccount", "accountManagementGranular", "canSendEnvelope", "canSendAPIRequests",
		"apiAccountWideAccess", "enableVaulting", "vaultingMode", "enableTransactionPoint",
		"enableSequentialSigningAPI", "enableSequentialSigningUI", "enableDSPro", "powerFormMode",
		"allowPowerFormsAdminToAccessAllPowerFormEnvelope", "canEditSharedAddressbook", "manageClickwrapsMode",
		"enableSignOnPaperOverride", "enableSignerAttachments", "allowSendOnBehalfOf", "canManageTemplates",
		"allowEnvelopeTransferTo", "allowRecipientLanguageSelection", "apiCanExportAC", "bulkSend", "canChargeAccount",
		"canManageDistributor", "canSignEnvelope", "newSendUI", "recipientViewedNotification", "templateActiveCreation",
		"templateApplyNotify", "templateAutoMatching", "templateMatchingSensitivity", "templatePageLevelMatching",
		"transactionPointSiteNameURL", "transactionPointUserName", "timezoneOffset", "timezoneMask", "timezoneDST",
		"modifiedBy", "modifiedPage", "modifiedDate", "adminOnly", "selfSignedRecipientEmailDocument",
		"signerEmailNotifications", "senderEmailNotifications", "localePolicy", "locale", "canLockEnvelopes",
		"canUseScratchpad", "canCreateWorkspaces", "isWorkspaceParticipant", "allowEmailChange", "allowPasswordChange",
		"federatedStatus", "allowSupplementalDocuments", "supplementalDocumentsMustView",
		"supplementalDocumentsMustAccept", "supplementalDocumentsMustRead", "canManageOrganization",
		"anchorTagVersionedPlacementEnabled", "expressSendOnly", "supplementalDocumentIncludeInDownload",
		"disableDocumentUpload", "disableOtherActions", "useAccountServerForPasswordChange", "isCommentsParticipant",
		"useAccountServerForEmailChange", "allowEsealRecipients", "sealIdentifiers", "agreedToComments",
		"canUseSmartContracts" })
public class AccountUserSettings {

	@JsonProperty("canManageAccount")
	private String canManageAccount;
	@JsonProperty("accountManagementGranular")
	private AccountManagementGranular accountManagementGranular;
	@JsonProperty("canSendEnvelope")
	private String canSendEnvelope;
	@JsonProperty("canSendAPIRequests")
	private String canSendAPIRequests;
	@JsonProperty("apiAccountWideAccess")
	private String apiAccountWideAccess;
	@JsonProperty("enableVaulting")
	private String enableVaulting;
	@JsonProperty("vaultingMode")
	private String vaultingMode;
	@JsonProperty("enableTransactionPoint")
	private String enableTransactionPoint;
	@JsonProperty("enableSequentialSigningAPI")
	private String enableSequentialSigningAPI;
	@JsonProperty("enableSequentialSigningUI")
	private String enableSequentialSigningUI;
	@JsonProperty("enableDSPro")
	private String enableDSPro;
	@JsonProperty("powerFormMode")
	private String powerFormMode;
	@JsonProperty("allowPowerFormsAdminToAccessAllPowerFormEnvelope")
	private String allowPowerFormsAdminToAccessAllPowerFormEnvelope;
	@JsonProperty("canEditSharedAddressbook")
	private String canEditSharedAddressbook;
	@JsonProperty("manageClickwrapsMode")
	private String manageClickwrapsMode;
	@JsonProperty("enableSignOnPaperOverride")
	private String enableSignOnPaperOverride;
	@JsonProperty("enableSignerAttachments")
	private String enableSignerAttachments;
	@JsonProperty("allowSendOnBehalfOf")
	private String allowSendOnBehalfOf;
	@JsonProperty("canManageTemplates")
	private String canManageTemplates;
	@JsonProperty("allowEnvelopeTransferTo")
	private String allowEnvelopeTransferTo;
	@JsonProperty("allowRecipientLanguageSelection")
	private String allowRecipientLanguageSelection;
	@JsonProperty("apiCanExportAC")
	private String apiCanExportAC;
	@JsonProperty("bulkSend")
	private String bulkSend;
	@JsonProperty("canChargeAccount")
	private String canChargeAccount;
	@JsonProperty("canManageDistributor")
	private String canManageDistributor;
	@JsonProperty("canSignEnvelope")
	private String canSignEnvelope;
	@JsonProperty("newSendUI")
	private String newSendUI;
	@JsonProperty("recipientViewedNotification")
	private String recipientViewedNotification;
	@JsonProperty("templateActiveCreation")
	private String templateActiveCreation;
	@JsonProperty("templateApplyNotify")
	private String templateApplyNotify;
	@JsonProperty("templateAutoMatching")
	private String templateAutoMatching;
	@JsonProperty("templateMatchingSensitivity")
	private String templateMatchingSensitivity;
	@JsonProperty("templatePageLevelMatching")
	private String templatePageLevelMatching;
	@JsonProperty("transactionPointSiteNameURL")
	private String transactionPointSiteNameURL;
	@JsonProperty("transactionPointUserName")
	private String transactionPointUserName;
	@JsonProperty("timezoneOffset")
	private String timezoneOffset;
	@JsonProperty("timezoneMask")
	private String timezoneMask;
	@JsonProperty("timezoneDST")
	private String timezoneDST;
	@JsonProperty("modifiedBy")
	private String modifiedBy;
	@JsonProperty("modifiedPage")
	private String modifiedPage;
	@JsonProperty("modifiedDate")
	private String modifiedDate;
	@JsonProperty("adminOnly")
	private String adminOnly;
	@JsonProperty("selfSignedRecipientEmailDocument")
	private String selfSignedRecipientEmailDocument;
	@JsonProperty("signerEmailNotifications")
	private SignerEmailNotifications signerEmailNotifications;
	@JsonProperty("senderEmailNotifications")
	private SenderEmailNotifications senderEmailNotifications;
	@JsonProperty("localePolicy")
	private LocalePolicy localePolicy;
	@JsonProperty("locale")
	private String locale;
	@JsonProperty("canLockEnvelopes")
	private String canLockEnvelopes;
	@JsonProperty("canUseScratchpad")
	private String canUseScratchpad;
	@JsonProperty("canCreateWorkspaces")
	private String canCreateWorkspaces;
	@JsonProperty("isWorkspaceParticipant")
	private String isWorkspaceParticipant;
	@JsonProperty("allowEmailChange")
	private String allowEmailChange;
	@JsonProperty("allowPasswordChange")
	private String allowPasswordChange;
	@JsonProperty("federatedStatus")
	private String federatedStatus;
	@JsonProperty("allowSupplementalDocuments")
	private String allowSupplementalDocuments;
	@JsonProperty("supplementalDocumentsMustView")
	private String supplementalDocumentsMustView;
	@JsonProperty("supplementalDocumentsMustAccept")
	private String supplementalDocumentsMustAccept;
	@JsonProperty("supplementalDocumentsMustRead")
	private String supplementalDocumentsMustRead;
	@JsonProperty("canManageOrganization")
	private String canManageOrganization;
	@JsonProperty("anchorTagVersionedPlacementEnabled")
	private String anchorTagVersionedPlacementEnabled;
	@JsonProperty("expressSendOnly")
	private String expressSendOnly;
	@JsonProperty("supplementalDocumentIncludeInDownload")
	private String supplementalDocumentIncludeInDownload;
	@JsonProperty("disableDocumentUpload")
	private String disableDocumentUpload;
	@JsonProperty("disableOtherActions")
	private String disableOtherActions;
	@JsonProperty("useAccountServerForPasswordChange")
	private String useAccountServerForPasswordChange;
	@JsonProperty("isCommentsParticipant")
	private String isCommentsParticipant;
	@JsonProperty("useAccountServerForEmailChange")
	private String useAccountServerForEmailChange;
	@JsonProperty("allowEsealRecipients")
	private String allowEsealRecipients;
	@JsonProperty("sealIdentifiers")
	private List<Object> sealIdentifiers = null;
	@JsonProperty("agreedToComments")
	private String agreedToComments;
	@JsonProperty("canUseSmartContracts")
	private String canUseSmartContracts;

}