package com.docusign.report.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;

@Service
public class CLMOAuthService implements IAuthService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${app.clmAPIVersion}")
	private String clmAPIVersion;

	@Value("${app.clmAuthAPIVersion}")
	private String clmAuthAPIVersion;

	@Value("${app.clmAPIBaseUrl}")
	private String clmAPIBaseUrl;

	@Value("${app.clmAuthAPIBaseUrl}")
	private String clmAuthAPIBaseUrl;

	@Value("${app.clmAPIUserEndPoint}")
	private String clmAPIUserEndPoint;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.CLMAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		String fullUri = clmAuthAPIBaseUrl + AppConstants.FORWARD_SLASH + clmAuthAPIVersion + clmAPIUserEndPoint;

		String msgBody = "{\"client_id\":" + "\"" + authenticationRequest.getClientId() + "\"" + ",\"client_secret\":"
				+ "\"" + authenticationRequest.getClientSecret() + "\"" + "}";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");

		HttpEntity<String> httpEntity = new HttpEntity<>(msgBody, headers);
		return callAPI(AuthenticationResponse.class, fullUri, HttpMethod.POST, httpEntity);
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return clmAPIBaseUrl + AppConstants.FORWARD_SLASH + clmAPIVersion;
	}

	private <T> T callAPI(Class<T> returnType, String fullUri, HttpMethod httpMethod, HttpEntity<String> httpEntity) {

		ResponseEntity<T> responseEntity = restTemplate.exchange(fullUri, httpMethod, httpEntity, returnType);
		return responseEntity.getBody();
	}

}