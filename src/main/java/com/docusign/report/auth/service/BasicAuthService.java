package com.docusign.report.auth.service;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.exception.InvalidInputException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasicAuthService implements IAuthService {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.BASICAUTHAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		if (StringUtils.isEmpty(authenticationRequest.getUserName())) {

			log.error("UserName is null or empty for " + APICategoryType.BASICAUTHAPI + " service");
			throw new InvalidInputException("UserName is null for " + APICategoryType.BASICAUTHAPI + " service");
		}

		if (StringUtils.isEmpty(authenticationRequest.getPassword())) {

			log.error("Password is null or empty for " + APICategoryType.BASICAUTHAPI + " service");
			throw new InvalidInputException("Password is null for " + APICategoryType.BASICAUTHAPI + " service");
		}
		String auth = authenticationRequest.getUserName() + AppConstants.COLON + authenticationRequest.getPassword();
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());

		AuthenticationResponse authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setTokenType("Basic");
		authenticationResponse.setAccessToken(new String(encodedAuth));
		return authenticationResponse;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return authenticationRequest.getBaseUrl();
	}

}