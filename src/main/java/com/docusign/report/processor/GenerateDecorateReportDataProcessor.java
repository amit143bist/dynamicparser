package com.docusign.report.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.FailureCode;
import com.docusign.report.common.constant.FailureStep;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.db.controller.CoreCacheDataLogController;
import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.domain.CommonPathData;
import com.docusign.report.domain.DecorateOutput;
import com.docusign.report.domain.Filter;
import com.docusign.report.domain.OutputColumn;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.ReportData;
import com.docusign.report.domain.TableColumnMetaData;
import com.docusign.report.service.BatchDataService;
import com.docusign.report.service.ReportJDBCService;
import com.docusign.report.utils.ReportAppUtil;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenerateDecorateReportDataProcessor {

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private PathValueProcessor pathValueProcessor;

	@Autowired
	private FilterDataProcessor filterDataProcessor;

	@Autowired
	private ScriptEngineManager scriptEngineManager;

	@Autowired
	private PrepareAPICallProcessor prepareAPICallProcessor;

	@Autowired
	private CoreCacheDataLogController coreCacheDataLogController;

	@Autowired
	private JavascriptFunctionProcessor javascriptFunctionProcessor;

	public void generateAndSaveReportData(List<String> pathList, DocumentContext docContext,
			List<OutputColumn> outputColumnList, String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, TableColumnMetaData tableColumnMetaData, String batchId, String processId,
			String nextUri, PrepareDataAPI prepareAPI) {

		log.info(
				"generateAndSaveReportData called for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
				nextUri, accountId, batchId, processId);

		if (null != pathList && !pathList.isEmpty()) {

			List<List<ReportData>> reportRowsList = new ArrayList<List<ReportData>>(pathList.size());

			try {

				createRowDataForEachPath(pathList, docContext, outputColumnList, accountId, inputParams,
						pathConfiguration, batchId, processId, nextUri, reportRowsList);

				String primaryIds = reportJDBCService.saveReportData(reportRowsList, tableColumnMetaData, accountId,
						batchId, processId, nextUri, prepareAPI.getOutputApiPrimaryId());

				if (!StringUtils.isEmpty(primaryIds)) {

					log.info("Calling update for primaryIds -> {}", primaryIds);
					accountId = verifyAccountIdForDocuSign(accountId, prepareAPI);
					prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, inputParams, null, nextUri, batchId,
							String.class, processId, primaryIds);
				}

			} catch (ResourceNotSavedException exp) {

				log.error(
						"Exception {} occurred in saving generateAndSaveReportData in tableName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
						exp, tableColumnMetaData.getTableName(), reportRowsList.size(), accountId, batchId, processId,
						nextUri);
				exp.printStackTrace();
				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_105.toString(),
						exp.getMessage(), FailureStep.SAVING_REPORT_DATA_IN_DB.toString(), exp);
			}
		} else {

			log.info(
					"PathLists is empty, generateAndSaveReportData called for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					nextUri, accountId, batchId, processId);
		}

	}

	private String verifyAccountIdForDocuSign(String accountId, PrepareDataAPI dataPrepareAPI) {

		APICategoryType apiCategoryType = ReportAppUtil.getAPICategoryType(dataPrepareAPI.getApiCategory());

		if ((StringUtils.isEmpty(accountId) || AppConstants.NOT_AVAILABLE_CONST.equalsIgnoreCase(accountId))
				&& (apiCategoryType == APICategoryType.ESIGNAPI || apiCategoryType == APICategoryType.CLICKAPI
						|| apiCategoryType == APICategoryType.ROOMSAPI)) {

			PathParam accountIdParam = ReportAppUtil.findPathParam(dataPrepareAPI.getApiParams(),
					AppConstants.DS_ACCOUNT_ID);

			if (null != accountIdParam && !StringUtils.isEmpty(accountIdParam.getParamValue())) {

				accountId = accountIdParam.getParamValue();
			} else {

				log.error("AccountId is not properly set for apiId -> {} and apiCategory -> {}",
						dataPrepareAPI.getApiId(), dataPrepareAPI.getApiCategory());
				throw new InvalidInputException("AccountId is not properly set for apiId -> "
						+ dataPrepareAPI.getApiId() + " for apiCategory -> " + dataPrepareAPI.getApiCategory());
			}
		}
		return accountId;
	}

	private void createRowDataForEachPath(List<String> pathList, DocumentContext docContext,
			List<OutputColumn> outputColumnList, String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, String batchId, String processId, String nextUri,
			List<List<ReportData>> reportRowsList) {

		for (String path : pathList) {// envelopes[0]....

			List<ReportData> reportColumnsDataList = new ArrayList<ReportData>(pathList.size());
			findColumnsDataForEachPath(docContext, outputColumnList, accountId, inputParams, pathConfiguration, batchId,
					processId, nextUri, path, reportColumnsDataList);

			reportRowsList.add(reportColumnsDataList);
		}
	}

	private void findColumnsDataForEachPath(DocumentContext docContext, List<OutputColumn> outputColumnList,
			String accountId, Map<String, Object> inputParams, Configuration pathConfiguration, String batchId,
			String processId, String nextUri, String path, List<ReportData> reportColumnsDataList) {

		for (OutputColumn outputColumn : outputColumnList) {

			log.info(
					"In generateAndSaveReportData Path is {}, outputColumn name is {} and outputColumn path is {} for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					path, outputColumn.getColumnName(), outputColumn.getColumnPath(), nextUri, accountId, batchId,
					processId);

			CommonPathData commonPathData = new CommonPathData();
			commonPathData.setOuterPath(path);
			commonPathData.setColumnDataType(outputColumn.getColumnDataType());
			commonPathData.setColumnPath(outputColumn.getColumnPath());
			commonPathData.setColumnDataPattern(outputColumn.getColumnDataPattern());
			commonPathData.setArrayIndex(outputColumn.getColumnDataArrayIndex());
			commonPathData.setMapKey(outputColumn.getColumnDataMapKey());
			commonPathData.setOutputDataPattern(outputColumn.getColumnOutputDataPattern());
			commonPathData.setStartIndex(outputColumn.getStartIndex());
			commonPathData.setEndIndex(outputColumn.getEndIndex());
			commonPathData.setOutputDelimiter(outputColumn.getOutputDelimiter());
			commonPathData.setTimeZone(outputColumn.getTimeZone());

			Object pathValue = pathValueProcessor.evaluatePathValue(commonPathData, docContext, accountId, batchId,
					nextUri, inputParams);

			log.debug(
					"For columnName -> {}, calculated pathValue is -> {} for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					outputColumn.getColumnName(), pathValue, nextUri, accountId, batchId, processId);

			if (null != pathValue && null != outputColumn.getAssociatedData()
					&& !StringUtils.isEmpty(outputColumn.getAssociatedData().getApiUri())) {

				fetchAndCacheAssociatedData(accountId, inputParams, pathConfiguration, batchId, processId, nextUri,
						reportColumnsDataList, outputColumn, pathValue);

			} else {

				decorateAndCreateReportData(reportColumnsDataList, outputColumn, pathValue, accountId, batchId,
						processId, inputParams, nextUri);
			}

		}
	}

	private void fetchAndCacheAssociatedData(String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, String batchId, String processId, String nextUri,
			List<ReportData> reportColumnsDataList, OutputColumn outputColumn, Object pathValue) {

		Object associatedDataValue = null;
		if (null != inputParams
				&& AppConstants.APP_TRUE.equalsIgnoreCase((String) inputParams.get(AppConstants.REFRESH_DATA_BASE))) {

			log.info(
					"RefreshDataBase value is true for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					nextUri, accountId, batchId, processId);

			associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
					pathValue, batchId, processId, nextUri, reportColumnsDataList);

			coreCacheDataLogController.deleteByCacheKeyAndCacheReference((String) pathValue,
					outputColumn.getColumnName());

			if (null != associatedDataValue) {

				saveCacheData(pathValue, associatedDataValue, accountId, batchId, processId,
						outputColumn.getColumnName());
			}

		} else {

			try {

				if (AppConstants.APP_TRUE.equalsIgnoreCase(outputColumn.getAssociatedData().getSaveDataInCache())) {

					associatedDataValue = coreCacheDataLogController
							.findByCacheKeyAndCacheReference((String) pathValue, outputColumn.getColumnName()).getBody()
							.getCacheValue();

					log.info(
							"Fetched {} from cache for key {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
							associatedDataValue, pathValue, nextUri, accountId, batchId, processId);

				} else {

					associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
							pathValue, batchId, processId, nextUri, reportColumnsDataList);
				}

			} catch (ResourceNotFoundException exception) {

				log.info(
						"No cacheData exists for cacheKey -> {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
						pathValue, nextUri, accountId, batchId, processId);

				associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
						pathValue, batchId, processId, nextUri, reportColumnsDataList);

				try {

					CacheLogInformation cacheLogInformationFromValue = coreCacheDataLogController
							.findByCacheValueAndCacheReference((String) associatedDataValue,
									outputColumn.getColumnName())
							.getBody();

					cacheLogInformationFromValue.setCacheKey((String) pathValue);
					cacheLogInformationFromValue.setCacheReference(outputColumn.getColumnName());
					coreCacheDataLogController.updateCache(cacheLogInformationFromValue.getCacheId(),
							cacheLogInformationFromValue);

				} catch (ResourceNotFoundException innerException) {

					log.info(
							"No cacheData exists for cacheValue -> {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
							associatedDataValue, nextUri, accountId, batchId, processId);

					if (null != associatedDataValue) {

						saveCacheData(pathValue, associatedDataValue, accountId, batchId, processId,
								outputColumn.getColumnName());
					}
				}

			}
		}

		decorateAndCreateReportData(reportColumnsDataList, outputColumn, associatedDataValue, accountId, batchId,
				processId, inputParams, nextUri);
	}

	private void saveCacheData(Object pathValue, Object associatedDataValue, String accountId, String batchId,
			String processId, String cachereference) {

		log.info(
				"SaveCacheData called for cacheKey -> {} and cacheValue -> {} for accountId -> {}, batchId -> {} and processId -> {}",
				pathValue, associatedDataValue, accountId, batchId, processId);

		CacheLogInformation cacheLogInformation = null;
		cacheLogInformation = new CacheLogInformation();
		cacheLogInformation.setCacheKey((String) pathValue);
		cacheLogInformation.setCacheValue((String) associatedDataValue);
		cacheLogInformation.setCacheReference(cachereference);

		coreCacheDataLogController.saveCache(cacheLogInformation);
	}

	private ReportData decorateAndCreateReportData(List<ReportData> reportColumnsData, OutputColumn outputColumn,
			Object associatedDataValue, String accountId, String batchId, String processId,
			Map<String, Object> inputParams, String nextUri) {

		if (null != outputColumn.getDecorateOutput()) {

			return decorateOutput(outputColumn, associatedDataValue, reportColumnsData, accountId, batchId, processId,
					inputParams, nextUri);
		} else {

			log.info("OutputColumn is {} and pathValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
					outputColumn.getColumnName(), associatedDataValue, accountId, batchId, processId);

			ReportData reportData = new ReportData();
			reportData.setReportColumnName(outputColumn.getColumnName());

			if (null != associatedDataValue) {

				reportData.setReportColumnValue(associatedDataValue);
			} else {
				reportData.setReportColumnValue(null);
			}

			log.debug(
					"ReportColumnName is {} and ReportColumnValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
					reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);

			if (!StringUtils.isEmpty(reportData.getReportColumnName())) {

				reportColumnsData.add(reportData);
			} else {

				log.warn(
						"ReportColumnName() {} is null for value -> {} for accountId -> {}, batchId -> {} and processId -> {}",
						reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId,
						processId);
			}

			return reportData;
		}

	}

	private Object extractAssociatedData(String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, OutputColumn outputColumn, Object pathValue, String batchId,
			String processId, String nextUri, List<ReportData> reportColumnsData) {

		Object associatedValue = null;
		PrepareDataAPI dataPrepareAPI = outputColumn.getAssociatedData();
		try {

			String json = prepareAPICallProcessor.callPrepareAPI(dataPrepareAPI, accountId, inputParams, pathValue,
					batchId, String.class, processId);

			if (!StringUtils.isEmpty(json)) {

				List<String> assocAPIDataPathList = null;
				DocumentContext associatedDocContext = JsonPath.parse(json);
				List<Filter> commonFilters = dataPrepareAPI.getCommonFilters();

				if (null != commonFilters && !commonFilters.isEmpty()) {

					for (int i = 0; i < commonFilters.size(); i++) {

						if (i == 0 && null == assocAPIDataPathList) {

							assocAPIDataPathList = filterDataProcessor.createPathList(commonFilters.get(i),
									pathConfiguration, json, inputParams);
						} else {

							associatedValue = filterDataProcessor.processFilterData(pathConfiguration,
									assocAPIDataPathList, commonFilters.get(i), json, inputParams, associatedDocContext,
									accountId, batchId, nextUri, reportColumnsData);
						}
					}
				}

				// Test below Code for more columns from AssociatedData, for instance tabData
				if (null != assocAPIDataPathList && !assocAPIDataPathList.isEmpty()
						&& null != dataPrepareAPI.getOutputColumns() && !dataPrepareAPI.getOutputColumns().isEmpty()) {

					for (String path : assocAPIDataPathList) {

						findColumnsDataForEachPath(associatedDocContext, dataPrepareAPI.getOutputColumns(), accountId,
								inputParams, pathConfiguration, batchId, processId, nextUri, path, reportColumnsData);
					}
				}

				log.warn(
						"AssociatedValue {} is extracted from extractAssociatedData for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
						associatedValue, nextUri, accountId, batchId, processId);
			}

		} catch (Exception exp) {

			log.error(
					"Exception {} occurred in extracting associatedData for pathValue -> {}, nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {} from {}",
					exp.getCause(), pathValue, nextUri, accountId, batchId, processId, dataPrepareAPI.getApiUri());
		}

		return associatedValue;
	}

	private ReportData decorateOutput(OutputColumn outputColumn, Object pathValue, List<ReportData> reportColumnsData,
			String accountId, String batchId, String processId, Map<String, Object> inputParams, String nextUri) {

		ReportData reportData = new ReportData();

		log.info(
				"In decorateOutput OutputColumnName is {} for pathValue -> {} for accountId -> {}, batchId -> {} and processId -> {}",
				outputColumn.getColumnName(), pathValue, accountId, batchId, processId);

		DecorateOutput decorateOutput = outputColumn.getDecorateOutput();
		String functionExpression = decorateOutput.getOutputPatternExpression();

		ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");

		Object columnDecoratedValue = javascriptFunctionProcessor.evaluateJSFunctionExpression(inputParams,
				decorateOutput.getPathParams(), null, engine, functionExpression, pathValue, accountId, batchId,
				nextUri, reportColumnsData);

		reportData.setReportColumnName(outputColumn.getColumnName());
		reportData.setReportColumnValue(columnDecoratedValue);

		log.debug(
				"In decorateOutput ReportColumnName is {} and ReportColumnValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
				reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);

		if (!StringUtils.isEmpty(reportData.getReportColumnName())) {

			reportColumnsData.add(reportData);
		} else {

			log.warn(
					"In decorateOutput ReportColumnName() {} is null for value -> {} for accountId -> {}, batchId -> {} and processId -> {}",
					reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);
		}

		return reportData;
	}

}