package com.docusign.report.prepare.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.FailureCode;
import com.docusign.report.common.constant.FailureStep;
import com.docusign.report.common.constant.ValidationResult;
import com.docusign.report.common.exception.AsyncInterruptedException;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.domain.BatchResultInformation;
import com.docusign.report.domain.BatchStartParams;
import com.docusign.report.domain.Filter;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.ReportRunArgs;
import com.docusign.report.domain.TableColumnMetaData;
import com.docusign.report.dsapi.service.DSAccountService;
import com.docusign.report.processor.FilterDataProcessor;
import com.docusign.report.processor.GenerateDecorateReportDataProcessor;
import com.docusign.report.processor.PrepareAPICallProcessor;
import com.docusign.report.service.BatchDataService;
import com.docusign.report.service.PrepareReportDataService;
import com.docusign.report.service.ReportJDBCService;
import com.docusign.report.utils.DateTimeUtil;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDSData implements IPrepareData {

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	DSAccountService dsAccountService;

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private FilterDataProcessor filterDataProcessor;

	@Autowired
	private PrepareAPICallProcessor prepareAPICallProcessor;

	@Autowired
	PrepareReportDataService prepareReportDataService;

	@Autowired
	private GenerateDecorateReportDataProcessor generateReportDataProcessor;

	@Value("${app.authorization.userId}")
	String authUserId;

	Configuration docContextPathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	Configuration pathConfiguration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.AS_PATH_LIST,
			Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	@Override
	public BatchResultInformation startPrepareDataProcess(PrepareDataAPI prepareAPI) {

		List<CompletableFuture<String>> reportDataFutureAccountList = new ArrayList<CompletableFuture<String>>();

		String batchId = null;
		try {

			batchId = startPrepareDataProcessAsync(prepareAPI, reportDataFutureAccountList);

			log.info("Size of reportDataFutureAccountList in startPrepareDataProcess is {}",
					reportDataFutureAccountList.size());
			CompletableFuture.allOf(
					reportDataFutureAccountList.toArray(new CompletableFuture[reportDataFutureAccountList.size()]))
					.get();

			log.info(
					" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ All main threads (at batch level) of size {} covering all accounts in batchId -> {} are completed ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
					reportDataFutureAccountList.size(), batchId);

			if (batchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

				return batchDataService.finishBatchProcessData(prepareAPI.getApiRunArgs(),
						prepareAPI.getApiRunArgs().getBatchType(), batchId);
			} else {

				return closeNoDataAvailableBatch(batchId);
			}

		} catch (InterruptedException exp) {

			log.error("InterruptedException {} occurred in AbstractDSData.startPrepareDataProcess for batchId {}", exp,
					batchId);

			batchDataService.createFailureRecord(batchId, batchId, FailureCode.ERROR_101.toString(), exp.getMessage(),
					FailureStep.JOINING_ALL_ACCOUNT_FUTURE.toString(), exp);
			exp.printStackTrace();

			throw new AsyncInterruptedException(
					"InterruptedException " + exp + " occurred in AbstractDSData.startPrepareDataProcess for batchId "
							+ batchId + " message " + exp.getMessage());
		} catch (ExecutionException exp) {

			log.error(
					"ExecutionException {} occurred in AbstractDSData.startPrepareDataProcess for batchId {} and the cause is {}",
					exp, batchId, exp.getCause());
			batchDataService.createFailureRecord(batchId, batchId, FailureCode.ERROR_102.toString(), exp.getMessage(),
					FailureStep.JOINING_ALL_ACCOUNT_FUTURE.toString(), exp);
			exp.printStackTrace();

			throw new AsyncInterruptedException(
					"ExecutionException " + exp + " occurred in AbstractDSData.startPrepareDataProcess for batchId "
							+ batchId + " message " + exp.getMessage() + " cause is " + exp.getCause());
		}
	}

	private BatchResultInformation closeNoDataAvailableBatch(String batchId) {

		log.warn("No worker threads available in prepare flow for batchId -> {}, so closing the batch", batchId);

		batchDataService.finishBatchProcess(batchId, 0L);
		BatchResultInformation batchResultInformation = new BatchResultInformation();
		batchResultInformation.setBatchId(batchId);
		batchResultInformation.setBatchStatus(ValidationResult.NODATAAVAILABLE.toString());

		return batchResultInformation;
	}

	abstract String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList);

	@Override
	public CompletableFuture<String> callDSAPIForEachRecord(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs,
			String batchId, String accountId) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			try {

				List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList = new ArrayList<ConcurrentProcessLogDefinition>();
				List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList = new ArrayList<CompletableFuture<ConcurrentProcessLogDefinition>>();

				callDSAPIForEachRecordAsync(prepareAPI, apiRunArgs, batchId, accountId, reportDataFutureAccountPageList,
						concurrentProcessLogDefinitionList);

				CompletableFuture.allOf(reportDataFutureAccountPageList
						.toArray(new CompletableFuture[reportDataFutureAccountPageList.size()])).get();

				concurrentProcessLogDefinitionList.forEach(concurrentProcessLogDefinition -> {

					batchDataService
							.finishConcurrentProcessWithTotalRecords(concurrentProcessLogDefinition.getProcessId());

					log.info(
							" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Envelope Load Completed for all threads of size {} and processId {} for accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
							reportDataFutureAccountPageList.size(), concurrentProcessLogDefinition.getProcessId(),
							accountId, batchId);
				});

			} catch (InterruptedException exp) {

				log.error(
						"InterruptedException {} occurred in AbstractDSData.callDSAPIForEachRecord for accountId {} and batchId {}",
						exp, accountId, batchId);

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_101.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp);
				exp.printStackTrace();

				throw new AsyncInterruptedException("InterruptedException " + exp
						+ " occurred in AbstractDSData.callDSAPIForEachRecord for accountId " + accountId
						+ " and batchId {} " + batchId + " message " + exp.getMessage());
			} catch (ExecutionException exp) {

				log.error(
						"ExecutionException {} occurred in AbstractDSData.callDSAPIForEachRecord for accountId {} and batchId {}, and the cause is {}",
						exp, accountId, batchId, exp.getCause());

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_102.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp);

				exp.printStackTrace();

				throw new AsyncInterruptedException("ExecutionException " + exp
						+ " occurred in AbstractDSData.callDSAPIForEachRecord for accountId " + accountId
						+ " and batchId " + batchId + " message " + exp.getMessage() + " cause " + exp.getCause());
			}
			return "success";
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside AbstractDSData.callDSAPIForEachRecord and exp is {} for accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, accountId, batchId);

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_107.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp);

				exp.printStackTrace();

				return "exception";
			}

			return asyncStatus;
		});
	}

	abstract void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList);

	@Override
	public void callAPIAndProcessConfiguredRules(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI) {

		Map<String, Object> inputParams = prepareReportDataService.prepareInputParams(prepareAPI.getApiRunArgs());

		inputParams.put("inputAccountId", accountId);
		inputParams.put("inputUserId", userId);

		log.info(
				"Calling API asynchronously -> {} with inputParams -> {} for userId -> {}, accountId -> {} and batchId -> {}",
				prepareAPI.getApiUri(), inputParams, userId, accountId, batchId);

		TableColumnMetaData tableColumnMetaData = reportJDBCService.getTableColumns(prepareAPI.getApiDataTableName());

		String json = prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, inputParams, batchId, null,
				String.class, AppConstants.NOT_AVAILABLE_CONST);

		log.debug("json before parsing {} for userId -> {}, accountId -> {} and batchId -> {}", json, userId, accountId,
				batchId);
		if (!StringUtils.isEmpty(json)) {

			DocumentContext docContext = JsonPath.using(docContextPathConfiguration).parse(json);

			// Creating below concurrentProcessLogDefinition for each AccountId entry where
			// accountId is the groupId
			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = batchDataService
					.createConcurrentProcess(Long.valueOf(1), batchId, accountId, null, userId);

			if (!StringUtils.isEmpty(prepareAPI.getApiNextPaginationPath())) {

				String nextUri = docContext.read("$" + prepareAPI.getApiNextPaginationPath());

				reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId, inputParams,
						prepareAPI, json, pathConfiguration, docContext, tableColumnMetaData, batchId, nextUri,
						concurrentProcessLogDefinition.getProcessId(), userId));

				while (!StringUtils.isEmpty(nextUri)) {

					String paginationJson = prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, nextUri,
							batchId, String.class, null);

					DocumentContext paginationDocContext = JsonPath.using(docContextPathConfiguration)
							.parse(paginationJson);
					nextUri = paginationDocContext.read("$" + prepareAPI.getApiNextPaginationPath());

					reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId, inputParams,
							prepareAPI, paginationJson, pathConfiguration, paginationDocContext, tableColumnMetaData,
							batchId, nextUri, concurrentProcessLogDefinition.getProcessId(), userId));

				}

			} else {

				reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId, inputParams,
						prepareAPI, json, pathConfiguration, docContext, tableColumnMetaData, null, null,
						concurrentProcessLogDefinition.getProcessId(), userId));
			}

			concurrentProcessLogDefinitionList.add(concurrentProcessLogDefinition);
		}
	}

	private CompletableFuture<ConcurrentProcessLogDefinition> filterProcessedPagedDataAndTriggerSave(String accountId,
			Map<String, Object> inputParams, PrepareDataAPI prepareAPI, String json, Configuration pathConfiguration,
			DocumentContext docContext, TableColumnMetaData tableColumnMetaData, String batchId, String nextUri,
			String parentGroupId, String userId) {

		log.info(
				"About to call processFilterPrepareJsonData asynchronously for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {}",
				nextUri, parentGroupId, accountId, batchId);

		return CompletableFuture.supplyAsync((Supplier<ConcurrentProcessLogDefinition>) () -> {

			log.info(
					" $$$$$$$$$$$$$$$$$$$$ Inside processFilterPrepareJsonData.supplyAsync $$$$$$$$$$$$$$$$$$$$ for nextUri -> {}, parentGroupId -> {}, accountId -> {}, batchId -> {} and inputParams is {}",
					nextUri, parentGroupId, accountId, batchId, inputParams);

			Integer totalSetSize = Integer.parseInt(docContext.read("$" + prepareAPI.getApiTotalSetSizePath()));

			long batchSize = 0;
			List<String> pathList = null;
			List<Filter> commonFilters = prepareAPI.getCommonFilters();

			try {
				if (totalSetSize > 0 && null != commonFilters && !commonFilters.isEmpty()) {

					for (int i = 0; i < commonFilters.size(); i++) {

						if (i == 0 && null == pathList) {

							pathList = filterDataProcessor.createPathList(commonFilters.get(i), pathConfiguration, json,
									inputParams);
						} else {

							filterDataProcessor.processFilterData(pathConfiguration, pathList, commonFilters.get(i),
									json, inputParams, docContext, accountId, batchId, nextUri, null);
						}
					}

				}

			} catch (Exception exp) {

				if (!StringUtils.isEmpty(exp.getMessage()) && exp.getMessage().contains("No results")) {

					log.warn(
							" ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Exception {} occurred in processFilterPrepareJsonData, so No results will be generated for json -> {} nextUri -> {}, parentGroupId ->{}, accountId -> {} and batchId -> {} ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ",
							exp.getCause(), json, nextUri, parentGroupId, accountId, batchId);
					batchSize = 0;
				} else {

					batchSize = -1;

					if (StringUtils.isEmpty(nextUri)) {

						batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_107.toString(),
								exp.getMessage(), FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC.toString(), exp);
					} else {

						batchDataService.createFailureRecord(accountId + "_" + nextUri, batchId,
								FailureCode.ERROR_107.toString(), exp.getMessage(),
								FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC.toString(), exp);
					}

					log.error(
							" ------------------------------ Exception {} occurred in processFilterPrepareJsonData, so no Report will be generated for nextUri -> {}, parentGroupId ->{}, accountId -> {} and batchId -> {} ------------------------------ ",
							exp.getCause(), nextUri, parentGroupId, accountId, batchId);
					exp.printStackTrace();
				}

			}

			// Above code block generates final pathList from outerJSON processing to be
			// sent for column data processing

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = null;

			if (null != pathList && !pathList.isEmpty()) {

				concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(pathList.size()),
						batchId, parentGroupId, accountId, userId);

				generateReportDataProcessor.generateAndSaveReportData(pathList, docContext,
						prepareAPI.getOutputColumns(), accountId, inputParams, pathConfiguration, tableColumnMetaData,
						batchId, concurrentProcessLogDefinition.getProcessId(), nextUri, prepareAPI);
			} else {

				concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(batchSize),
						batchId, parentGroupId, accountId, userId);
				log.warn(
						" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ PathList {} is empty, so no Report will be generated for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {} and processId -> {} @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ",
						pathList, nextUri, parentGroupId, accountId, batchId,
						concurrentProcessLogDefinition.getProcessId());
			}

			return concurrentProcessLogDefinition;
		}, recordTaskExecutor).thenApplyAsync(concurrentProcessLogDefinition -> {

			log.info(
					" %%%%%%%%%%%%%%%%%%%% Inside processFilterPrepareJsonData.applyAsync %%%%%%%%%%%%%%%%%%%% for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {}  and processId -> {}",
					nextUri, parentGroupId, accountId, batchId, concurrentProcessLogDefinition.getProcessId());

			batchDataService.finishConcurrentProcess(concurrentProcessLogDefinition.getProcessId());

			return concurrentProcessLogDefinition;

		}, recordTaskExecutor).handle((concurrentProcessLogDefinition, exp) -> {

			log.info(
					" ^^^^^^^^^^^^^^^^^^^^ Inside processFilterPrepareJsonData.handleAsync ^^^^^^^^^^^^^^^^^^^^ for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {}",
					nextUri, parentGroupId, accountId, batchId);

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside processFilterPrepareJsonData.handleAsync and exp is {} for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, nextUri, parentGroupId, accountId, batchId);

				batchDataService.createFailureRecord(accountId + "_" + nextUri, batchId,
						FailureCode.ERROR_107.toString(), exp.getMessage(),
						FailureStep.OUTER_JSON_FILTER_PROCESSING.toString(), exp);

				exp.printStackTrace();
			} else if (null != concurrentProcessLogDefinition) {

				log.info(
						"No Exception occurred in processFilterPrepareJsonData.handleAsync in processing data for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {} and processId -> {}",
						parentGroupId, accountId, batchId, concurrentProcessLogDefinition.getProcessId());

			} else {

				log.warn(
						" !!!!!!!!!!!!!!!!!!!! Something went wrong, No Exception occurred in processFilterPrepareJsonData.handleAsync nor there are any processes created !!!!!!!!!!!!!!!!!!!!");
			}

			return concurrentProcessLogDefinition;
		});
	}

	@Override
	public String createBatchRecord(ReportRunArgs apiRunArgs, List<String> recordIds) {

		String batchType = apiRunArgs.getBatchType();

		Map<String, Object> inputParams = prepareReportDataService.prepareInputParams(apiRunArgs);

		BatchStartParams batchStartParams = new BatchStartParams();

		if (null != inputParams.get(AppConstants.INPUT_FROM_DATE)) {

			batchStartParams.setBeginDateTime(DateTimeUtil
					.convertToEpochTimeFromDateTimeAsString((String) inputParams.get(AppConstants.INPUT_FROM_DATE)));
		}

		if (null != inputParams.get(AppConstants.INPUT_TO_DATE)) {

			batchStartParams.setEndDateTime(DateTimeUtil
					.convertToEpochTimeFromDateTimeAsString((String) inputParams.get(AppConstants.INPUT_TO_DATE)));
		}

		String recordIdsCommaSeparated = null;
		if (null != recordIds && !recordIds.isEmpty()) {

			batchStartParams.setTotalRecordIds(recordIds.size());

			recordIdsCommaSeparated = recordIds.stream().collect(Collectors.joining(","));
		}

		String batchId = batchDataService.createBatchJob(batchType, batchStartParams, null);

		log.info(
				" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Running batchId -> {} for inputStartDate -> {}, inputToDate -> {} and accountIds -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
				batchId, inputParams.get(AppConstants.INPUT_FROM_DATE), inputParams.get(AppConstants.INPUT_TO_DATE),
				recordIdsCommaSeparated);
		return batchId;
	}

}