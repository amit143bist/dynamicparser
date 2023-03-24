package com.docusign.report.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.common.constant.APICategoryType;

@Service
public class OrgAdminOAuthService extends AbstractDSAuthService implements IAuthService {

	@Value("${app.orgAdminAPIBaseUrl}")
	private String orgAdminAPIBaseUrl;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ORGADMINAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return orgAdminAPIBaseUrl;
	}

}