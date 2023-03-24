package com.docusign.report.processor;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.docusign.report.common.constant.PathParamDataType;
import com.docusign.report.domain.PathParam;
import com.docusign.report.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BindingParamDataProcessor {

	public void populateBindingParams(Map<String, Object> inputParams, List<PathParam> pathParams, Bindings bindings,
			String parentData, String nextUri, String accountId, String batchId) {

		log.info(
				"InputParams -> {}, parentData -> {}, nextUri -> {}, accountId -> {}, batchId -> {} and PathParams - {}",
				inputParams, parentData, nextUri, accountId, batchId, pathParams);

		for (PathParam pathParam : pathParams) {

			Object paramValue = null;

			if (pathParam.getParamName().contains("input")) {

				paramValue = inputParams.get(pathParam.getParamName());
			} else if (!StringUtils.isEmpty(pathParam.getParamValue())) {

				paramValue = pathParam.getParamValue();
			} else if (!StringUtils.isEmpty(parentData)) {

				paramValue = pathParam.getParamValue();
			}

			if (!StringUtils.isEmpty(pathParam.getParamDataType())) {

				PathParamDataType pathParamDataTypeEnum = EnumUtils.getEnum(PathParamDataType.class,
						pathParam.getParamDataType().toUpperCase());
				switch (pathParamDataTypeEnum) {

				case DATETIMEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) paramValue,
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
				default:
					log.warn(
							"<<<<<<<<<<<<<<<<<<<< Wrong ParamDataType -> {} in populateBindingParams >>>>>>>>>>>>>>>>>>>>",
							pathParam.getParamDataType());
				}
			}

			bindings.put(pathParam.getParamName(), paramValue);
		}
	}
}