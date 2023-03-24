package com.docusign.report.dsapi.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.docusign.report.domain.ApiHourlyLimitData;
import com.docusign.report.dsapi.domain.DocumentResponse;
import com.docusign.report.dsapi.domain.EnvelopeDocument;
import com.docusign.report.service.PrepareReportDataService;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSEnvelopeService {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	DSAuthorizationCache dsAuthorizationCache;

	@Autowired
	PrepareReportDataService prepareReportDataService;

	@Value("${app.esignAPIDocumentsEndpoint}")
	private String esignAPIDocumentsEndpoint;

	public List<EnvelopeDocument> fetchAllDocuments(AuthenticationRequest authenticationRequest, String envelopeId) {

		log.debug("Retrieving documents from accountId -> {} for envelopeId -> {}",
				authenticationRequest.getAccountGuid(), envelopeId);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		Assert.notNull(authenticationResponse, "AuthenticationResponse is empty");
		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional
					.ofNullable(restTemplate.exchange(dsAuthorizationCache.requestBaseUrl(authenticationRequest)
							+ "/accounts/" + authenticationRequest.getAccountGuid() + "/envelopes/" + envelopeId + "/"
							+ esignAPIDocumentsEndpoint, HttpMethod.GET, httpEntity, DocumentResponse.class))
					.map(documentResponse -> {

						Assert.isTrue(documentResponse.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(documentResponse.getBody(), "documentResponse is null");
						prepareReportDataService.readApiHourlyLimitData(documentResponse.getHeaders());

						return documentResponse.getBody().getEnvelopeDocuments();

					}).orElseThrow(() -> new ResourceNotFoundException(
							"documentResponse not retured for envelopeId " + envelopeId));
		} catch (HttpClientErrorException exp) {

			ApiHourlyLimitData apiHourlyLimitData = prepareReportDataService
					.readApiHourlyLimitData(exp.getResponseHeaders());

			if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

				log.info("For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
						apiHourlyLimitData.getDocuSignTraceToken());
			}

			log.info(
					"Calling DSEnvelopeService.fetchAllDocuments: Receive HttpClientErrorException {}, responseBody -> {}",
					exp.getStatusCode(), exp.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to call DSEnvelopeService.fetchAllDocuments", exp);

		}
	}

}