package com.docusign.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.ReportRunArgs;

@Component
public class PrepareBasicAuthData extends AbstractDSData {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.BASICAUTHAPI == apiCategoryType || APICategoryType.NOAUTHAPI == apiCategoryType;
	}

	@Override
	String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList) {

		ReportRunArgs apiRunArgs = prepareAPI.getApiRunArgs();
		String batchId = createBatchRecord(apiRunArgs, null);

		reportDataFutureAccountList
				.add(callDSAPIForEachRecord(prepareAPI, apiRunArgs, batchId, AppConstants.NOT_AVAILABLE_CONST));

		return batchId;
	}

	@Override
	void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList) {

		callAPIAndProcessConfiguredRules(accountId, batchId, reportDataFutureAccountPageList,
				AppConstants.NOT_AVAILABLE_CONST, concurrentProcessLogDefinitionList, prepareAPI);
	}

}