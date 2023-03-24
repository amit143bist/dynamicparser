package com.docusign.report.auth.service;

import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.constant.APICategoryType;

@Service
public class NoAuthService implements IAuthService {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.NOAUTHAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		return null;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return authenticationRequest.getBaseUrl();
	}

}