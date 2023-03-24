package com.docusign.report.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.docusign.report.auth.factory.AuthenticationFactory;
import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.constant.ValidationResult;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSAuthorizationCache {

	@Autowired
	CacheManager cacheManager;

	@Autowired
	AuthenticationFactory authenticationFactory;

	@Cacheable(value = "token", key = "#authenticationRequest.accountGuid + '_' + #authenticationRequest.apiCategory", sync = true)
	public AuthenticationResponse requestToken(AuthenticationRequest authenticationRequest) {

		return authenticationFactory.fetchAuthServiceByAPICategoryType(
				ReportAppUtil.getAPICategoryType(authenticationRequest.getApiCategory())).map(authService -> {

					return authService.requestOAuthToken(authenticationRequest);
				}).get();
	}

	@Cacheable(value = "baseUrl", key = "#authenticationRequest.accountGuid + '_' + #authenticationRequest.apiCategory", sync = true)
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		log.info("Fetching baseUrl for accountId -> {}", authenticationRequest.getAccountGuid());

		return authenticationFactory.fetchAuthServiceByAPICategoryType(
				ReportAppUtil.getAPICategoryType(authenticationRequest.getApiCategory())).map(authService -> {

					return authService.requestBaseUrl(authenticationRequest);
				}).get();
	}

	public String clearCache() {

		log.info("ClearCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();

		return ValidationResult.SUCCESS.toString();
	}

	/**
	 * Clear all tokens from token cache, every
	 */
	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	public void evictAuthenticationCache() {

		log.info("EvictAuthenticationCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();
	}
}