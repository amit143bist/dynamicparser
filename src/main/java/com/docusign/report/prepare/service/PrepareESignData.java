package com.docusign.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.ReportRunArgs;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrepareESignData extends AbstractDSData {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ESIGNAPI == apiCategoryType;
	}

	@Override
	public String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList) {

		ReportRunArgs apiRunArgs = prepareAPI.getApiRunArgs();
		prepareReportDataService.validateReportRunArgs(apiRunArgs);
		List<String> accountIds = prepareReportDataService.getAllAccountIds(apiRunArgs, prepareAPI.getApiCategory(), prepareAPI.getApiId());

		String batchId = createBatchRecord(apiRunArgs, accountIds);

		accountIds.forEach(accountId -> {

			reportDataFutureAccountList.add(callDSAPIForEachRecord(prepareAPI, apiRunArgs, batchId, accountId));
		});

		return batchId;
	}

	@Override
	void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList) {

		PathParam processAllUserPathParam = ReportAppUtil.findPathParam(prepareAPI.getApiRunArgs().getPathParams(),
				AppConstants.PROCESS_ALL_USERS_FLAG);

		if (null != processAllUserPathParam && !StringUtils.isEmpty(processAllUserPathParam.getParamValue())
				&& "true".equalsIgnoreCase(processAllUserPathParam.getParamValue())) {// If true, process API
																						// call for
																						// each user

			List<String> accountUserIdList = prepareReportDataService.getAllUserIds(apiRunArgs, accountId,
					prepareAPI.getApiCategory(), prepareAPI.getApiId());

			accountUserIdList.forEach(accountUserId -> {

				filterAndTriggerAPICall(accountId, batchId, reportDataFutureRecordPageList, accountUserId,
						concurrentProcessLogDefinitionList, prepareAPI);
			});

		} else {

			filterAndTriggerAPICall(accountId, batchId, reportDataFutureRecordPageList, authUserId,
					concurrentProcessLogDefinitionList, prepareAPI);
		}

	}

	private void filterAndTriggerAPICall(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI) {

		log.debug("Inside callDSAPI for accountId -> {}, batchId -> {} and userId -> {}", accountId, batchId, userId);
		// Do not process the DataPrepareAPI if accountId is one of the filterAccountIds

		List<String> filterAccountIdList = null;
		String filterAccountIds = prepareAPI.getFilterAccountIds();
		if (!StringUtils.isEmpty(filterAccountIds)) {

			filterAccountIdList = Stream.of(filterAccountIds.split(",")).map(String::trim).collect(Collectors.toList());
		}

		if (null != filterAccountIdList && !filterAccountIdList.isEmpty() && filterAccountIdList.contains(accountId)) {

			return;
		}
		// Do not process the DataPrepareAPI if accountId is one of the filterAccountIds

		callAPIAndProcessConfiguredRules(accountId, batchId, reportDataFutureAccountPageList, userId,
				concurrentProcessLogDefinitionList, prepareAPI);
	}

}