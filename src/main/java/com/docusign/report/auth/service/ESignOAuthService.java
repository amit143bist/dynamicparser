package com.docusign.report.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.dsapi.service.DSAccountService;

@Service
public class ESignOAuthService extends AbstractDSAuthService implements IAuthService {

	@Autowired
	DSAccountService dsAccountService;

	@Value("${app.esignAPIVersion}")
	private String esignAPIVersion;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ESIGNAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return dsAccountService.getUserInfo(authenticationRequest.getAccountGuid(), authenticationRequest)
				+ AppConstants.FORWARD_SLASH + "restapi" + AppConstants.FORWARD_SLASH + esignAPIVersion;
	}

}