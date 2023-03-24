package com.docusign.report.processor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.docusign.report.domain.CommonPathData;
import com.docusign.report.domain.Filter;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.ReportData;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilterDataProcessor {

	@Autowired
	private PathValueProcessor pathValueProcessor;

	@Autowired
	private ScriptEngineManager scriptEngineManager;

	@Autowired
	private BindingParamDataProcessor bindingParamDataProcessor;

	@Autowired
	private JavascriptFunctionProcessor javascriptFunctionProcessor;

	public List<String> createPathList(Filter filter, Configuration pathConfiguration, String json,
			Map<String, Object> inputParams) {

		if (null == filter.getPathParams() || filter.getPathParams().isEmpty()) {

			return JsonPath.using(pathConfiguration).parse(json).read(filter.getPath());
		} else {

			String filterPath = filter.getPath();
			List<PathParam> pathParamList = filter.getPathParams();

			for (PathParam pathParam : pathParamList) {

				Object paramValue = null;

				if (pathParam.getParamName().contains("input")) {

					paramValue = inputParams.get(pathParam.getParamName());
				} else if (!StringUtils.isEmpty(pathParam.getParamValue())) {

					paramValue = pathParam.getParamValue();
				}

				switch (pathParam.getParamDataType()) {

				case "Text":

					paramValue = "'" + paramValue + "'";
					filterPath = filterPath.replace(pathParam.getParamName(), (String) paramValue);
					break;
				case "Integer":

					filterPath = filterPath.replace(pathParam.getParamName(), (String) paramValue);
					break;
				default:
					log.warn("<<<<<<<<<<<<<<<<<<<< Wrong ParamDataType -> {} in createPathList >>>>>>>>>>>>>>>>>>>>",
							pathParam.getParamDataType());

				}
			}

			return JsonPath.using(pathConfiguration).parse(json).read(filterPath);
		}

	}

	public Object processFilterData(Configuration pathConfiguration, List<String> pathList, Filter filter, String json,
			Map<String, Object> inputParams, DocumentContext docContext, String accountId, String batchId,
			String nextUri, List<ReportData> reportColumnsData) {

		log.info(
				"ProcessFilterData called for pathList {} with filterId {} and filterPath {}, inputParams -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				pathList, filter.getFilterId(), filter.getPath(), inputParams, nextUri, accountId, batchId);

		Object pathValue = null;

		if (null != pathList && !pathList.isEmpty()) {

			Iterator<String> pathIterator = pathList.iterator();

			while (pathIterator.hasNext()) {

				String outerPath = pathIterator.next();

				log.info(
						"OuterPath is ---> {} with filterId {} and filterPath {} for nextUri -> {}, accountId -> {} and batchId -> {} ",
						outerPath, filter.getFilterId(), filter.getPath(), nextUri, accountId, batchId);

				if (StringUtils.isEmpty(filter.getEvaluateValue())
						|| "true".equalsIgnoreCase(filter.getEvaluateValue())) {

					CommonPathData commonPathData = new CommonPathData();
					commonPathData.setOuterPath(outerPath);
					commonPathData.setColumnDataType(filter.getPathOutputDataType());
					commonPathData.setColumnPath(filter.getPath());
					commonPathData.setColumnDataPattern(filter.getPathInputDataPattern());
					commonPathData.setArrayIndex(filter.getPathOutputDataArrayIndex());
					commonPathData.setMapKey(filter.getPathOutputDataMapKey());
					commonPathData.setOutputDataPattern(filter.getPathOutputDataPattern());
					commonPathData.setStartIndex(filter.getStartIndex());
					commonPathData.setEndIndex(filter.getEndIndex());
					commonPathData.setOutputDelimiter(filter.getOutputDelimiter());
					commonPathData.setTimeZone(filter.getTimeZone());

					pathValue = pathValueProcessor.evaluatePathValue(commonPathData, docContext, accountId, batchId,
							nextUri, inputParams);
				}

				String expression = filter.getExpression();
				if (!StringUtils.isEmpty(expression)) {

					Boolean expressionEvalResult = false;
					ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");

					if (expression.contains("function")) {

						expressionEvalResult = javascriptFunctionProcessor.evaluateJSFunction(inputParams,
								filter.getPathParams(), null, engine, expression, pathValue, accountId, batchId,
								nextUri, reportColumnsData);

					} else {

						Bindings bindings = engine.createBindings();
						bindings.put("$", pathValue);

						bindingParamDataProcessor.populateBindingParams(inputParams, filter.getPathParams(), bindings,
								null, nextUri, accountId, batchId);
						try {

							expressionEvalResult = (Boolean) engine.eval(expression, bindings);
						} catch (ScriptException e) {

							e.printStackTrace();
						}
					}

					log.info(
							"ExpressionEvalResult value is {} for outerPath {}, filterPath {} and pathValue {} for nextUri -> {}, accountId -> {} and batchId -> {}",
							expressionEvalResult, outerPath, filter.getPath(), pathValue, nextUri, accountId, batchId);

					if (null != expressionEvalResult && expressionEvalResult) {

						pathIterator.remove();
					}
				}
			}
		}

		return pathValue;
	}

}