package com.docusign.report.auth.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;

@Service
public class RoomsOAuthService extends AbstractDSAuthService implements IAuthService {

	@Value("${app.roomsAPIBaseUrl}")
	private String roomsAPIBaseUrl;

	@Value("${app.roomsAPIVersion}")
	private String roomsAPIVersion;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ROOMSAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		if (StringUtils.isEmpty(authenticationRequest.getAccountGuid())) {

			return roomsAPIBaseUrl + AppConstants.FORWARD_SLASH + roomsAPIVersion;
		} else {

			return roomsAPIBaseUrl + AppConstants.FORWARD_SLASH + roomsAPIVersion + AppConstants.FORWARD_SLASH
					+ authenticationRequest.getAccountGuid();
		}
	}

}