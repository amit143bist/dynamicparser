package com.docusign.report.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.docusign.report.domain.BatchResultInformation;
import com.docusign.report.domain.BatchResultResponse;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.PrepareReportDefinition;
import com.docusign.report.prepare.factory.PrepareDataFactory;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CorePrepareReportDataController {

	@Autowired
	PrepareDataFactory prepareDataFactory;

	@PostMapping("/report/preparereportdata")
	public ResponseEntity<BatchResultResponse> prepareReportData(
			@RequestBody PrepareReportDefinition prepareReportDefinition, @RequestHeader Map<String, String> headers) {

		return callAPIWithInputParams(prepareReportDefinition, headers);

	}

	private ResponseEntity<BatchResultResponse> callAPIWithInputParams(PrepareReportDefinition prepareReportDefinition,
			Map<String, String> headers) {

		List<BatchResultInformation> batchResultInformationList = new ArrayList<BatchResultInformation>();
		for (PrepareDataAPI prepareAPI : prepareReportDefinition.getPrepareDataAPIs()) {

			printRunModeByBatchType(headers, prepareAPI.getApiRunArgs().getBatchType());

			prepareDataFactory.prepareData(ReportAppUtil.getAPICategoryType(prepareAPI.getApiCategory()))
					.map(prepareDataService -> {

						return batchResultInformationList.add(prepareDataService.startPrepareDataProcess(prepareAPI));
					});

		}

		BatchResultResponse batchResultResponse = new BatchResultResponse();
		batchResultResponse.setBatchResultInformations(batchResultInformationList);

		return new ResponseEntity<BatchResultResponse>(batchResultResponse, HttpStatus.CREATED);

	}

	private void printRunModeByBatchType(Map<String, String> headers, String batchType) {

		if (null != headers && null != headers.get("accept")) {

			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING " + batchType
					+ " IN ONLINE MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
		} else {

			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING " + batchType
					+ " IN BATCH MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
		}
	}

}