package com.docusign.report.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.docusign.report.domain.CommonPathData;
import com.docusign.report.utils.PathValueUtil;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PathValueProcessor {
	
	/*
	 * public Object evaluatePathValue(String outerPath, String columnDataType,
	 * String columnPath, String columnDataPattern, DocumentContext docContext,
	 * String accountId, String batchId, String nextUri, Map<String, Object>
	 * inputParams) {
	 * 
	 * return evaluatePathValue(outerPath, columnDataType, columnPath,
	 * columnDataPattern, null, null, docContext, accountId, batchId, nextUri,
	 * inputParams); }
	 * 
	 * public Object evaluatePathValue(String outerPath, String columnDataType,
	 * String columnPath, String columnDataPattern, Integer arrayIndex, String
	 * mapKey, DocumentContext docContext, String accountId, String batchId, String
	 * nextUri, Map<String, Object> inputParams) {
	 * 
	 * if (null == columnPath) {
	 * 
	 * return columnPath; }
	 * 
	 * Object pathValue = null; String fullPath = outerPath + columnPath;
	 * 
	 * log.info(
	 * "OuterPath -> {}, columnDataType -> {}, columnPath -> {}, columnDataPattern -> {} and fullPath -> {} for nextUri -> {}, accountId -> {} and batchId -> {}"
	 * , outerPath, columnDataType, columnPath, columnDataPattern, fullPath,
	 * nextUri, accountId, batchId);
	 * 
	 * if (StringUtils.isEmpty(columnDataType)) {
	 * 
	 * columnDataType = "Text"; }
	 * 
	 * ColumnDataType columnDataTypeEnum = EnumUtils.getEnum(ColumnDataType.class,
	 * columnDataType.toUpperCase());
	 * 
	 * if (null != columnPath && null == docContext.read(fullPath) &&
	 * columnDataTypeEnum != ColumnDataType.INPUT) {
	 * 
	 * return pathValue; }
	 * 
	 * switch (columnDataTypeEnum) {
	 * 
	 * case DATE:
	 * 
	 * pathValue =
	 * DateTimeUtil.convertToEpochTimeFromDateAsString(docContext.read(fullPath),
	 * columnDataPattern); break; case DATETIME:
	 * 
	 * pathValue =
	 * DateTimeUtil.convertToEpochTimeFromDateTimeAsString(docContext.read(fullPath)
	 * , columnDataPattern); break; case TEXT:
	 * 
	 * pathValue = docContext.read(fullPath); break; case NUMBER:
	 * 
	 * pathValue = Long.parseLong(docContext.read(fullPath)); break; case LENGTH:
	 * 
	 * pathValue = Long.valueOf(((String) docContext.read(fullPath)).length());
	 * break; case ARRAY:
	 * 
	 * List<Object> objectArray = docContext.read(fullPath); pathValue =
	 * objectArray;
	 * 
	 * break; case TEXTFROMARRAY:
	 * 
	 * List<Object> objectTextArray = docContext.read(fullPath); pathValue =
	 * objectTextArray.get(arrayIndex);
	 * 
	 * break; case ARRAYMAP:
	 * 
	 * List<Map<String, Object>> objectArrayMap = docContext.read(fullPath);
	 * pathValue = objectArrayMap; break; case TEXTFROMARRAYMAP:
	 * 
	 * List<Map<String, Object>> objectTextArrayMap = docContext.read(fullPath);
	 * pathValue = objectTextArrayMap.get(arrayIndex).get(mapKey); break; case
	 * ARRAYSIZE:
	 * 
	 * List<Object> objectList = docContext.read(fullPath); pathValue =
	 * Long.valueOf(objectList.size());
	 * 
	 * break; case MAP:
	 * 
	 * Map<String, Object> objectMap = docContext.read(fullPath); pathValue =
	 * objectMap; break; case INPUT:
	 * 
	 * pathValue = inputParams.get(columnPath); break; default: log.
	 * warn(":::::::::::::::::::: Invalid Case in evaluatePathValue() -> {} ::::::::::::::::::::"
	 * , columnDataType); }
	 * 
	 * return pathValue; }
	 */
	
	public Object evaluatePathValue(CommonPathData commonPathData, DocumentContext docContext, String accountId,
			String batchId, String nextUri, Map<String, Object> inputParams) {

		if (null == commonPathData.getColumnPath()) {

			return commonPathData.getColumnPath();
		}

		log.debug(
				"OuterPath -> {}, columnDataType -> {}, columnPath -> {}, columnDataPattern -> {} and fullPath -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				commonPathData.getOuterPath(), commonPathData.getColumnDataType(), commonPathData.getColumnPath(),
				commonPathData.getColumnDataPattern(), commonPathData.getOuterPath() + commonPathData.getColumnPath(),
				nextUri, accountId, batchId);

		return PathValueUtil.evaluateValueFromJSONPath(commonPathData, docContext, inputParams);
	}

}