package com.docusign.report.auth.service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.constant.APICategoryType;

public interface IAuthService {

	boolean canHandleRequest(APICategoryType apiCategoryType);

	AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest);

	String requestBaseUrl(AuthenticationRequest authenticationRequest);
}