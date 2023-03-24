package com.docusign.report.dsapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.AuthenticationResponse;
import com.docusign.report.cache.DSAuthorizationCache;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.dsapi.domain.Account;
import com.docusign.report.dsapi.domain.Organization;
import com.docusign.report.dsapi.domain.Organizations;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrgAdminService {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	DSAuthorizationCache dsAuthorizationCache;

	public List<String> getAllAccountIds(String organizationId, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving organization information for organizationId = {} " + organizationId);

		Assert.notNull(organizationId, "organizationId was empty");

		Organization organization = getOrganization(organizationId, authenticationRequest);

		List<String> accountIds = new ArrayList<String>();

		if (null != organization) {

			List<Account> accounts = organization.getAccounts();

			accounts.forEach(account -> {

				accountIds.add(account.getId());
			});
		}

		return accountIds;
	}

	public Organization getOrganization(String organizationId, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving organization information for organizationId = {} " + organizationId);

		Assert.notNull(organizationId, "organizationId was empty");

		Organizations organizations = getOrganization(authenticationRequest);

		for (Organization organization : organizations.getOrganizations()) {

			if (StringUtils.equalsIgnoreCase(organizationId, organization.getId())) {

				return organization;
			}
		}
		return null;
	}

	public Organizations getOrganization(AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving organization account information.");

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		Assert.notNull(authenticationResponse, "AuthenticationResponse is empty");

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional.ofNullable(restTemplate.exchange(dsAuthorizationCache.requestBaseUrl(authenticationRequest),
					HttpMethod.GET, httpEntity, Organizations.class)).map(organizationId -> {

						Assert.isTrue(organizationId.getStatusCode().is2xxSuccessful(),
								"Docusign organization data is not returned with 2xx status code");
						Assert.notNull(organizationId.getBody(), "Organizations is null");
						return organizationId.getBody();

					})
					.orElseThrow(() -> new ResourceNotFoundException(
							"Org admin API exception when retrieving organizations administered by organizationAdmin "
									+ authenticationRequest.getUserId()));
		} catch (HttpClientErrorException e) {

			log.info(
					"Calling OrgAdminService.getOrganizations: Receive HttpClientErrorException {}, responseBody -> {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to retrieve organization", e);

		}

	}
}