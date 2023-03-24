package com.docusign.report.dsapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.cache.DSAuthorizationCache;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.domain.ApiHourlyLimitData;
import com.docusign.report.dsapi.domain.AccountUser;
import com.docusign.report.dsapi.domain.AccountUserInformation;
import com.docusign.report.dsapi.domain.UserAccount;
import com.docusign.report.dsapi.domain.UserInformation;
import com.docusign.report.service.PrepareReportDataService;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSAccountService {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	DSAuthorizationCache dsAuthorizationCache;

	@Autowired
	PrepareReportDataService prepareReportDataService;

	@Value("${app.authorization.userId}")
	private String userId;

	@Value("${app.authorization.aud}")
	private String accountServiceBaseUrl;

	@Value("${app.esignAPIAccountUsersEndpoint}")
	private String esignAPIAccountUsersEndpoint;

	public List<String> getAllAccountIds(AuthenticationRequest authenticationRequest) {

		UserInformation userInformation = getUserInfo(authenticationRequest);

		List<String> accountIds = new ArrayList<String>();
		if (null != userInformation) {

			List<UserAccount> userAccounts = userInformation.getUserAccounts();

			userAccounts.forEach(account -> {

				accountIds.add(account.getAccountId());
			});
		}

		return accountIds;
	}

	public UserInformation getUserInfo(AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving userinfo account information.");

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(
				dsAuthorizationCache.requestToken(authenticationRequest), MediaType.APPLICATION_JSON_VALUE,
				MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional.ofNullable(restTemplate.exchange("https://" + accountServiceBaseUrl + "/oauth/userinfo",
					HttpMethod.GET, httpEntity, UserInformation.class)).map(userInfo -> {

						Assert.isTrue(userInfo.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(userInfo.getBody(), "UserInfo is null");
						return userInfo.getBody();

					})
					.orElseThrow(() -> new ResourceNotFoundException(
							"UserInfo not retured for accountAdmin " + null != authenticationRequest.getUserId()
									? authenticationRequest.getUserId()
									: userId));
		} catch (HttpClientErrorException e) {

			log.info("Calling DSAccountService.getUserInfo: Receive HttpClientErrorException {}, responseBody -> {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to retrieve userinfo", e);

		}
	}

	public String getUserInfo(String accountGuid, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving userinfo account information for accountId -> {}", accountGuid);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);
		Assert.notNull(authenticationResponse, "AuthenticationResponse is empty");

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional.ofNullable(restTemplate.exchange("https://" + accountServiceBaseUrl + "/oauth/userinfo",
					HttpMethod.GET, httpEntity, UserInformation.class)).map(userInfo -> {

						Assert.isTrue(userInfo.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(userInfo.getBody(), "UserInfo is null");

						UserInformation userInformation = userInfo.getBody();
						if (null != userInformation && null != userInformation.getUserAccounts()
								&& !userInformation.getUserAccounts().isEmpty()) {

							List<UserAccount> userAccountList = userInformation.getUserAccounts();
							for (UserAccount userAccount : userAccountList) {

								if (accountGuid.equalsIgnoreCase(userAccount.getAccountId())) {

									return userAccount.getBaseUri();
								}
							}
						}

						return null;

					})
					.orElseThrow(() -> new ResourceNotFoundException(
							"UserInfo not retured for accountAdmin " + null != authenticationRequest.getUserId()
									? authenticationRequest.getUserId()
									: userId));
		} catch (HttpClientErrorException e) {

			log.info("Calling DSAccountService.getUserInfo: Receive HttpClientErrorException {}, responseBody -> {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to retrieve userinfo", e);

		}
	}

	public List<AccountUser> fetchAllAccountUsers(String accountGuid, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving all account users from accountId -> {}", accountGuid);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		String baseUrl = dsAuthorizationCache.requestBaseUrl(authenticationRequest);

		List<AccountUser> accountUserList = new ArrayList<AccountUser>();
		try {
			Optional.ofNullable(
					restTemplate.exchange(baseUrl + "/accounts/" + accountGuid + "/" + esignAPIAccountUsersEndpoint,
							HttpMethod.GET, httpEntity, AccountUserInformation.class))
					.map(accountUserResponse -> {

						Assert.isTrue(accountUserResponse.getStatusCode().is2xxSuccessful(),
								"Account userInfo data is not returned with 2xx status code");
						Assert.notNull(accountUserResponse.getBody(), "accountUserResponse is null");

						accountUserList.addAll(accountUserResponse.getBody().getUsers());

						prepareReportDataService.readApiHourlyLimitData(accountUserResponse.getHeaders());
						processPaginationData(accountGuid, baseUrl, httpEntity, accountUserList, accountUserResponse);

						return accountUserList;

					}).orElseThrow(() -> new ResourceNotFoundException(
							"accountUserResponse not retured for accountId " + accountGuid));
		} catch (HttpClientErrorException exp) {

			ApiHourlyLimitData apiHourlyLimitData = prepareReportDataService
					.readApiHourlyLimitData(exp.getResponseHeaders());

			if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

				log.info("For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
						apiHourlyLimitData.getDocuSignTraceToken());
			}

			log.info(
					"Calling DSAccountService.fetchAllAccountUsers: Receive HttpClientErrorException {}, responseBody -> {}",
					exp.getStatusCode(), exp.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to call DSAccountService.fetchAllAccountUsers", exp);

		}

		return accountUserList;
	}

	private void processPaginationData(String accountGuid, String baseUrl, HttpEntity<String> httpEntity,
			List<AccountUser> accountUserList, ResponseEntity<AccountUserInformation> accountUserResponse) {

		String nextUri = accountUserResponse.getBody().getNextUri();

		while (!StringUtils.isEmpty(nextUri)) {

			ResponseEntity<AccountUserInformation> accountUserInformationResponse = restTemplate.exchange(
					baseUrl + "/accounts/" + accountGuid + "/" + nextUri, HttpMethod.GET, httpEntity,
					AccountUserInformation.class);

			nextUri = accountUserInformationResponse.getBody().getNextUri();
			accountUserList.addAll(accountUserInformationResponse.getBody().getUsers());
			prepareReportDataService.readApiHourlyLimitData(accountUserResponse.getHeaders());
		}
	}
}