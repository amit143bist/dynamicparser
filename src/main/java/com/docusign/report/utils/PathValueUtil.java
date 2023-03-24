package com.docusign.report.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.ColumnDataType;
import com.docusign.report.domain.CommonPathData;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathValueUtil {

	public static Object evaluateValueFromJSONPath(CommonPathData commonPathData, DocumentContext docContext,
			Map<String, Object> inputParams) {

		log.debug("Calling evaluateValueFromJSONPath for commonPathData -> {}", commonPathData);
		Object pathValue = null;
		String fullPath = commonPathData.getOuterPath() + commonPathData.getColumnPath();

		if (StringUtils.isEmpty(commonPathData.getColumnDataType())) {

			commonPathData.setColumnDataType("Text");
		}

		ColumnDataType columnDataTypeEnum = EnumUtils.getEnum(ColumnDataType.class,
				commonPathData.getColumnDataType().toUpperCase());

		if (null != commonPathData.getColumnPath() && null == docContext.read(fullPath)
				&& columnDataTypeEnum != ColumnDataType.INPUT) {

			return pathValue;
		}

		switch (columnDataTypeEnum) {

		case DATEFROMEPOCHTIME:

			pathValue = getDateAsStringFromEpochTime(commonPathData, docContext.read(fullPath));
			break;
		case DATETIMEFROMEPOCHTIME:

			pathValue = getDateTimeAsStringFromEpochTime(commonPathData, docContext.read(fullPath));
			break;
		case DATEASEPOCHTIME:

			pathValue = getDateAsEpochTime(commonPathData, docContext.read(fullPath));
			break;
		case DATETIMEASEPOCHTIME:

			pathValue = getDateTimeAsEpochTime(commonPathData, docContext.read(fullPath));
			break;
		case DATE:

			pathValue = getDateAsString(commonPathData, docContext.read(fullPath));

			break;
		case DATETIME:

			pathValue = getDateTimeAsString(commonPathData, docContext.read(fullPath));
			break;
		case TEXT:

			pathValue = docContext.read(fullPath);
			break;
		case TEXTSUBSTR:

			pathValue = docContext.read(fullPath);

			pathValue = substringText(commonPathData, pathValue);
			break;
		case NUMBER:

			if (null != docContext.read(fullPath)) {

				pathValue = Long.parseLong(docContext.read(fullPath));
			}
			break;
		case LENGTH:

			if (null != docContext.read(fullPath)) {

				pathValue = Long.valueOf(((String) docContext.read(fullPath)).length());
			}
			break;
		case ARRAY:

			List<Object> objectArray = docContext.read(fullPath);
			pathValue = objectArray;
			break;
		case ARRAYCONCAT:

			List<Object> origObjectArray = docContext.read(fullPath);

			String delimiter = commonPathData.getOutputDelimiter();

			if (StringUtils.isEmpty(delimiter)) {
				delimiter = AppConstants.COMMA_DELIMITER;
			}
			if (null != origObjectArray && !origObjectArray.isEmpty()) {

				pathValue = origObjectArray.stream().filter(s -> (null != s)).map(String::valueOf)
						.filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.joining(delimiter));
			}
			break;

		case ARRAYTOTAL:

			List<Object> oriObjectArray = docContext.read(fullPath);

			if (null != oriObjectArray && !oriObjectArray.isEmpty()) {

				pathValue = oriObjectArray.stream().filter(s -> (null != s)).map(String::valueOf)
						.filter(s -> !StringUtils.isEmpty(s)).map(s -> Integer.parseInt(s)).collect(Collectors.toList())
						.stream().mapToInt(Integer::intValue).sum();

			}

			break;
		case ARRAYSIZE:

			List<Object> objectList = docContext.read(fullPath);

			if (null != objectList) {

				pathValue = Long.valueOf(objectList.size());
			}
			break;
		case TEXTFROMARRAY:

			pathValue = getTextFromArray(commonPathData, docContext, pathValue, fullPath);
			break;
		case DATEFROMARRAY:

			pathValue = getTextFromArray(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateAsString(commonPathData, pathValue);

			break;
		case DATETIMEFROMARRAY:

			pathValue = getTextFromArray(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateTimeAsString(commonPathData, pathValue);

			break;
		case DATEASEPOCHTIMEFROMARRAY:

			pathValue = getTextFromArray(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateAsEpochTime(commonPathData, pathValue);
			break;

		case DATETIMEASEPOCHTIMEFROMARRAY:

			pathValue = getTextFromArray(commonPathData, docContext, pathValue, fullPath);

			if (null != pathValue) {

				pathValue = getDateTimeAsEpochTime(commonPathData, pathValue);
			}
			break;
		case ARRAYMAP:

			List<Map<String, Object>> objectArrayMap = docContext.read(fullPath);
			pathValue = objectArrayMap;
			break;
		case TEXTFROMARRAYMAP:

			pathValue = getTextFromArrayMap(commonPathData, docContext, pathValue, fullPath);

			break;
		case DATEFROMARRAYMAP:

			pathValue = getTextFromArrayMap(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateAsString(commonPathData, pathValue);
			break;
		case DATETIMEFROMARRAYMAP:

			pathValue = getTextFromArrayMap(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateTimeAsString(commonPathData, pathValue);
			break;
		case DATEASEPOCHTIMEFROMARRAYMAP:

			pathValue = getTextFromArrayMap(commonPathData, docContext, pathValue, fullPath);

			if (null != pathValue) {

				pathValue = getDateAsEpochTime(commonPathData, pathValue);
			}
			break;
		case DATETIMEASEPOCHTIMEFROMARRAYMAP:

			pathValue = getTextFromArrayMap(commonPathData, docContext, pathValue, fullPath);

			if (null != pathValue) {

				pathValue = getDateTimeAsEpochTime(commonPathData, pathValue);
			}
			break;
		case MAP:

			Map<String, Object> objectMap = docContext.read(fullPath);
			pathValue = objectMap;
			break;
		case TEXTFROMMAP:

			pathValue = getTextFromMap(commonPathData, docContext, pathValue, fullPath);

			break;
		case DATEFROMMAP:

			pathValue = getTextFromMap(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateAsString(commonPathData, pathValue);

			break;

		case DATETIMEFROMMAP:

			pathValue = getTextFromMap(commonPathData, docContext, pathValue, fullPath);
			pathValue = getDateTimeAsString(commonPathData, pathValue);
			break;
		case DATEASEPOCHTIMEFROMMAP:

			pathValue = getTextFromMap(commonPathData, docContext, pathValue, fullPath);
			if (null != pathValue && !StringUtils.isEmpty(commonPathData.getColumnDataPattern())) {

				pathValue = getDateAsEpochTime(commonPathData, pathValue);
			}
			break;

		case DATETIMEASEPOCHTIMEFROMMAP:

			pathValue = getTextFromMap(commonPathData, docContext, pathValue, fullPath);
			if (null != pathValue && !StringUtils.isEmpty(commonPathData.getColumnDataPattern())) {

				pathValue = getDateTimeAsEpochTime(commonPathData, pathValue);
			}
			break;
		case INPUT:

			if (null != inputParams) {

				pathValue = inputParams.get(commonPathData.getColumnPath());
			}
			break;
		default:
			log.warn(":::::::::::::::::::: Invalid Case in evaluateValueFromPath() -> {} ::::::::::::::::::::",
					commonPathData.getColumnDataType());
		}

		return pathValue;
	}

	private static Object getDateTimeAsString(CommonPathData commonPathData, Object pathValue) {

		String inputDataDateTimePattern = commonPathData.getColumnDataPattern();
		if (StringUtils.isEmpty(inputDataDateTimePattern)) {

			inputDataDateTimePattern = DateTimeUtil.DATE_TIME_PATTERN_NANO;
		}

		if (null != pathValue) {

			if (null != commonPathData.getOutputDataPattern()) {

				LocalDateTime ldateTime = LocalDateTime.parse((String) pathValue,
						DateTimeFormatter.ofPattern(inputDataDateTimePattern));
				pathValue = DateTimeFormatter.ofPattern(commonPathData.getOutputDataPattern()).format(ldateTime);

			} else {

				pathValue = DateTimeUtil.convertToSQLDateTimeFromDateTimeAsString((String) pathValue,
						inputDataDateTimePattern);
			}

		}
		return pathValue;
	}

	private static Object getDateAsString(CommonPathData commonPathData, Object pathValue) {

		String inputDateTimePattern = commonPathData.getColumnDataPattern();
		if (StringUtils.isEmpty(inputDateTimePattern)) {

			inputDateTimePattern = DateTimeUtil.DATE_TIME_PATTERN_NANO;
		}

		if (null != pathValue) {

			if (null != commonPathData.getOutputDataPattern()) {

				LocalDate ldateTime = LocalDate.parse((String) pathValue,
						DateTimeFormatter.ofPattern(inputDateTimePattern));

				pathValue = DateTimeFormatter.ofPattern(commonPathData.getOutputDataPattern()).format(ldateTime);
			} else {

				pathValue = DateTimeUtil.convertToSQLDateFromDateTimeAsString((String) pathValue, inputDateTimePattern);
			}
		}
		return pathValue;
	}

	private static Object getTextFromMap(CommonPathData commonPathData, DocumentContext docContext, Object pathValue,
			String fullPath) {

		Map<String, Object> objectTextMap = docContext.read(fullPath);

		if (null != objectTextMap) {

			pathValue = objectTextMap.get(commonPathData.getMapKey());

			pathValue = substringText(commonPathData, pathValue);
		}
		return pathValue;
	}

	private static Object getTextFromArrayMap(CommonPathData commonPathData, DocumentContext docContext,
			Object pathValue, String fullPath) {

		List<Map<String, Object>> objectTextArrayMap = docContext.read(fullPath);

		if (null != objectTextArrayMap && !objectTextArrayMap.isEmpty()) {

			if (null == commonPathData.getArrayIndex()) {

				pathValue = objectTextArrayMap.get(0).get(commonPathData.getMapKey());
			} else {

				pathValue = objectTextArrayMap.get(commonPathData.getArrayIndex()).get(commonPathData.getMapKey());
			}

			pathValue = substringText(commonPathData, pathValue);
		}
		return pathValue;
	}

	private static Object getDateTimeAsEpochTime(CommonPathData commonPathData, Object pathValue) {

		if (null != pathValue) {

			pathValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) pathValue,
					commonPathData.getColumnDataPattern());
		}

		if (null != pathValue) {

			pathValue = DateTimeUtil.convertToLocalDateFromEpochTimeInSecs((Long) pathValue,
					commonPathData.getTimeZone(), commonPathData.getOutputDataPattern());
		}
		return pathValue;
	}

	private static Object getDateAsEpochTime(CommonPathData commonPathData, Object pathValue) {

		if (null != pathValue) {

			pathValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) pathValue,
					commonPathData.getColumnDataPattern());
		}

		if (null != pathValue) {

			pathValue = DateTimeUtil.convertToLocalDateFromEpochTimeInSecs((Long) pathValue,
					commonPathData.getTimeZone(), commonPathData.getOutputDataPattern());
		}
		return pathValue;
	}

	private static Object getTextFromArray(CommonPathData commonPathData, DocumentContext docContext, Object pathValue,
			String fullPath) {

		List<Object> objectTextArray = docContext.read(fullPath);

		if (null != objectTextArray && !objectTextArray.isEmpty()) {

			if (null == commonPathData.getArrayIndex()) {

				// Remove empty or null elements
				List<String> newStringList = objectTextArray.stream().filter(s -> (null != s)).map(String::valueOf)
						.filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());

				if (null != newStringList && !newStringList.isEmpty()) {

					pathValue = newStringList.get(0);
				}
			} else {

				pathValue = objectTextArray.get(commonPathData.getArrayIndex());
			}

			pathValue = substringText(commonPathData, pathValue);
		}

		return pathValue;
	}

	private static Object substringText(CommonPathData commonPathData, Object pathValue) {

		if (null != pathValue) {

			Integer beginIndex = 0;
			Integer endIndex = ((String) pathValue).length();
			if (null != commonPathData.getStartIndex()) {

				beginIndex = commonPathData.getStartIndex();
			}

			if (null != commonPathData.getEndIndex()) {

				endIndex = commonPathData.getEndIndex();
			}

			if (beginIndex < endIndex) {

				pathValue = ((String) pathValue).substring(beginIndex, endIndex);
			}

		}
		return pathValue;
	}

	private static Object getDateAsStringFromEpochTime(CommonPathData commonPathData, Object pathValue) {

		if (null != pathValue) {

			String className = pathValue.getClass().getName();

			log.debug("ClassName type for pathValue -> is -> {} for columnPath -> {}", pathValue, className,
					commonPathData.getColumnPath());
			if ("java.lang.Long".equalsIgnoreCase(className)) {

				pathValue = DateTimeUtil.convertToLocalDateFromEpochTimeInSecs((Long) pathValue,
						commonPathData.getTimeZone(), commonPathData.getOutputDataPattern());
			} else if ("java.sql.Date".equalsIgnoreCase(className)) {

				pathValue = extraDateFromSQLDate(commonPathData, pathValue);
			}
		}

		return pathValue;
	}

	private static Object getDateTimeAsStringFromEpochTime(CommonPathData commonPathData, Object pathValue) {

		if (null != pathValue) {

			String className = pathValue.getClass().getName();

			log.debug("ClassName type for pathValue -> is -> {} for columnPath -> {}", pathValue, className,
					commonPathData.getColumnPath());
			if ("java.lang.Long".equalsIgnoreCase(className)) {

				pathValue = DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs((Long) pathValue,
						commonPathData.getTimeZone(), commonPathData.getOutputDataPattern());
			} else if ("java.sql.Date".equalsIgnoreCase(className)) {

				pathValue = extraDateFromSQLDate(commonPathData, pathValue);
			}
		}

		return pathValue;
	}

	private static Object extraDateFromSQLDate(CommonPathData commonPathData, Object pathValue) {

		DateFormat sourceFormat = new SimpleDateFormat(commonPathData.getColumnDataPattern());
		DateFormat targetFormat = new SimpleDateFormat(commonPathData.getOutputDataPattern());

		try {

			Date date = sourceFormat.parse(((java.sql.Date) pathValue).toString());
			pathValue = targetFormat.format(date);
		} catch (ParseException e) {

			log.error("Cannot parse value -> {} from sourceFormat -> {} to targetFormat -> {}", pathValue,
					commonPathData.getColumnDataPattern(), commonPathData.getOutputDataPattern());
			e.printStackTrace();
		}
		return pathValue;
	}
}