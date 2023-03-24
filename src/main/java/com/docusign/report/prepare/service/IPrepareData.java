package com.docusign.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.domain.BatchResultInformation;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.ReportRunArgs;

public interface IPrepareData {

	boolean canHandleRequest(APICategoryType apiCategoryType);

	BatchResultInformation startPrepareDataProcess(PrepareDataAPI prepareAPI);

	CompletableFuture<String> callDSAPIForEachRecord(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs,
			String batchId, String accountId);

	void callAPIAndProcessConfiguredRules(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI);
	
	String createBatchRecord(ReportRunArgs apiRunArgs, List<String> recordIds);

}