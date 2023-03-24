package com.docusign.report.auth.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.common.exception.ConsentRequiredException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.dsapi.service.DSAccountService;
import com.docusign.report.utils.JWTUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDSAuthService implements IAuthService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	DSAccountService dsAccountService;

	@Value("${app.authorization.aud}")
	private String aud;

	@Value("${app.authorization.userId}")
	private String userId;

	@Value("${app.authorization.scopes}")
	private String scopes;

	@Value("${app.authorization.integratorKey}")
	private String integratorKey;

	@Value("${app.authorization.token.expirationSeconds}")
	private String expirationSeconds;

	@Value("${app.authorization.rsaPrivateKeyPath}")
	private String rsaPrivateKeyPath;

	@Value("${app.authorization.rsaPublicKeyPath}")
	private String rsaPublicKeyPath;

	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {
		// pass the path to the file as a parameter

		AtomicReference<String> jwtUserId = new AtomicReference<String>();
		String jwtScopes = scopes;
		try {

			log.info("Generating Access Token in requestOauthToken method for accountId {}",
					authenticationRequest.getAccountGuid());

			if (!StringUtils.isEmpty(authenticationRequest.getUserId())) {

				jwtUserId.set(authenticationRequest.getUserId());
			} else {
				jwtUserId.set(userId);
			}

			if (!StringUtils.isEmpty(authenticationRequest.getScopes())) {

				jwtScopes = authenticationRequest.getScopes();
			}

			String assertion = JWTUtils.generateJWTAssertion(rsaPublicKeyPath, rsaPrivateKeyPath, aud, integratorKey,
					jwtUserId.get(), Long.valueOf(expirationSeconds), jwtScopes);

			log.info("JWT assertion {} for user {} with IntegratorKey {} and accountId {}", assertion, jwtUserId.get(),
					integratorKey, authenticationRequest.getAccountGuid());

			Assert.notNull(assertion, "assertion was empty");

			MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
			form.add("assertion", assertion);
			form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setCacheControl(CacheControl.noStore());
			headers.setPragma("no-cache");

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
			return Optional.ofNullable(

					restTemplate.exchange("https://" + aud + "/oauth/token", HttpMethod.POST, request,
							AuthenticationResponse.class))
					.map(authenticationToken -> {

						Assert.notNull(authenticationToken.getBody(),
								"authenticationToken is null for user " + jwtUserId.get());
						Assert.isTrue(authenticationToken.getStatusCode().is2xxSuccessful(),
								"AuthenticationToken is not returned with 200 status code");

						log.debug("Returning Access Token -> {} for user {} with IntegratorKey {} and accountId {}",
								authenticationToken, jwtUserId.get(), integratorKey,
								authenticationRequest.getAccountGuid());

						return authenticationToken.getBody();
					}).orElseThrow(
							() -> new ResourceNotFoundException("Token was not retrieved from authenticationToken"));
		} catch (HttpClientErrorException e) {

			log.error(
					"HttpClientErrorException {} happened in generating Access Token for user {} with IntegratorKey {} and accountId {}",
					e.getMessage(), jwtUserId.get(), integratorKey, authenticationRequest.getAccountGuid());

			log.error("HttpClientErrorException statusCode is {} and ResponseBody is  {}", e.getStatusCode(),
					e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.BAD_REQUEST
					&& e.getResponseBodyAsString().contains("consent_required")) {

				String consentUrl = "https://" + aud + "/oauth/auth?response_type=code&scope=" + scopes + "&client_id="
						+ integratorKey + "redirect_uri=https://www.docusign.com";
				throw new ConsentRequiredException(
						"Unable to Obtain token for user: " + jwtUserId.get() + ". " + "Error description: "
								+ e.getResponseBodyAsString() + " Obtain token by launching " + consentUrl);
			}

			throw e;
		} catch (IOException e) {

			log.error("IOException {} happened in generating Access Token for user {} with IntegratorKey {}",
					e.getMessage(), jwtUserId.get(), integratorKey);

			throw new ConsentRequiredException("Unable to read key " + e.getMessage());
		}
	}
}