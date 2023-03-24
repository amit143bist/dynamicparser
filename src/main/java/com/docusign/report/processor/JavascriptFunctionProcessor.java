package com.docusign.report.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.docusign.report.common.constant.FailureCode;
import com.docusign.report.common.constant.FailureStep;
import com.docusign.report.common.constant.PathParamDataType;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.ReportData;
import com.docusign.report.service.BatchDataService;
import com.docusign.report.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavascriptFunctionProcessor {

	@Autowired
	BatchDataService batchDataService;

	public Boolean evaluateJSFunction(Map<String, Object> inputParams, List<PathParam> pathParams, String parentData,
			ScriptEngine engine, String expression, Object pathValue, String accountId, String batchId, String nextUri,
			List<ReportData> reportColumnsData) {

		log.info(
				"In evaluateJSFunction inputParams -> {}, pathParams -> {}, parentData -> {}, expression -> {} and pathValue -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				inputParams, pathParams, parentData, expression, pathValue, nextUri, accountId, batchId);

		return extractJSExpression(inputParams, pathParams, parentData, engine, expression, pathValue, accountId,
				batchId, nextUri, reportColumnsData, Boolean.class);
	}

	public Object evaluateJSFunctionExpression(Map<String, Object> inputParams, List<PathParam> pathParams,
			String parentData, ScriptEngine engine, String expression, Object pathValue, String accountId,
			String batchId, String nextUri, List<ReportData> reportColumnsData) {

		log.info(
				"In evaluateJSFunctionExpression inputParams -> {}, pathParams -> {}, parentData -> {}, expression -> {} and pathValue -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				inputParams, pathParams, parentData, expression, pathValue, nextUri, accountId, batchId);

		return extractJSExpression(inputParams, pathParams, parentData, engine, expression, pathValue, accountId,
				batchId, nextUri, reportColumnsData, Object.class);

	}

	private <T> T extractJSExpression(Map<String, Object> inputParams, List<PathParam> pathParams, String parentData,
			ScriptEngine engine, String expression, Object pathValue, String accountId, String batchId, String nextUri,
			List<ReportData> reportColumnsData, Class<T> returnType) {

		T expressionResult = null;

		List<Object> objectFunctionParamList = new ArrayList<Object>(pathParams.size());

		if (null != pathValue && !StringUtils.isEmpty(pathValue)) {

			objectFunctionParamList.add(pathValue);
		}

		String functionName = extractPathParams(inputParams, pathParams, parentData, objectFunctionParamList,
				reportColumnsData);

		try {

			engine.eval(expression);
			Invocable invocable = (Invocable) engine;

			if (null != objectFunctionParamList && !objectFunctionParamList.isEmpty()) {

				Object[] objectFunctionParamArr = new Object[objectFunctionParamList.size()];
				expressionResult = returnType.cast(invocable.invokeFunction(functionName,
						objectFunctionParamList.toArray(objectFunctionParamArr)));
			} else {
				expressionResult = returnType.cast(invocable.invokeFunction(functionName));
			}

		} catch (NoSuchMethodException exp) {

			log.error(
					"NoSuchMethodException thrown for functionName {}, nextUri -> {}, accountId -> {} and batchId -> {}",
					functionName, nextUri, accountId, batchId);
			batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_103.toString(), exp.getMessage(),
					FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp);
			exp.printStackTrace();
		} catch (ScriptException exp) {

			log.error("ScriptException thrown for functionName {}, nextUri -> {}, accountId -> {} and batchId -> {}",
					functionName, nextUri, accountId, batchId);
			batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_103.toString(), exp.getMessage(),
					FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp);
			exp.printStackTrace();
		}
		return expressionResult;
	}

	private ReportData findReportData(List<ReportData> reportColumnsData, String paramName) {

		return reportColumnsData.stream()
				.filter(columnsData -> paramName.equalsIgnoreCase(columnsData.getReportColumnName())).findAny()
				.orElse(null);
	}

	private String extractPathParams(Map<String, Object> inputParams, List<PathParam> pathParams, String parentData,
			List<Object> objectFunctionParamList, List<ReportData> reportColumnsData) {

		String functionName = null;
		for (PathParam pathParam : pathParams) {

			if ("functionName".equalsIgnoreCase(pathParam.getParamName())) {

				functionName = pathParam.getParamValue();
				continue;
			}

			Object paramValue = null;
			ReportData reportData = null;

			if (null != reportColumnsData && !reportColumnsData.isEmpty()) {

				reportData = findReportData(reportColumnsData, pathParam.getParamName());
			}

			if (pathParam.getParamName().contains("input")) {

				paramValue = inputParams.get(pathParam.getParamName());
			} else if (!StringUtils.isEmpty(pathParam.getParamValue())) {

				paramValue = pathParam.getParamValue();
			} else if (null != reportData) {

				paramValue = reportData.getReportColumnValue();
			} else if (!StringUtils.isEmpty(parentData)) {

				paramValue = parentData;
			}

			if (!StringUtils.isEmpty(pathParam.getParamDataType()) && !StringUtils.isEmpty(paramValue)) {

				PathParamDataType pathParamDataTypeEnum = EnumUtils.getEnum(PathParamDataType.class,
						pathParam.getParamDataType().toUpperCase());

				switch (pathParamDataTypeEnum) {

				case DATEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATETIMEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATETIME:

					paramValue = DateTimeUtil.convertToSQLDateTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATE:

					paramValue = DateTimeUtil.convertToSQLDateFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case ARRAY:
					String[] paramArray = ((String) paramValue).split(",");
					paramValue = paramArray;
					break;
				default:
					log.warn(
							"<<<<<<<<<<<<<<<<<<<< Wrong ParamDataType -> {} in evaluateJSFunction >>>>>>>>>>>>>>>>>>>>",
							pathParam.getParamDataType());

				}
			}

			if (!StringUtils.isEmpty(paramValue)) {

				objectFunctionParamList.add(paramValue);
			}

		}
		return functionName;
	}
}