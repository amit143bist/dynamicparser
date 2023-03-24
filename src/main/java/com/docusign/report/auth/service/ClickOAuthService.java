package com.docusign.report.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;

@Service
public class ClickOAuthService extends AbstractDSAuthService implements IAuthService {

	@Value("${app.roomsAPIBaseUrl}")
	private String roomsAPIBaseUrl;

	@Value("${app.clickAPIVersion}")
	private String clickAPIVersion;

	@Value("${app.clickAPIEndPoint}")
	private String clickAPIEndPoint;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.CLICKAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return dsAccountService.getUserInfo(authenticationRequest.getAccountGuid(), authenticationRequest)
				+ AppConstants.FORWARD_SLASH + clickAPIEndPoint + AppConstants.FORWARD_SLASH + clickAPIVersion;
	}

}