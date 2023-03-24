package com.docusign.report.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.DecorateOutputType;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.domain.DecorateOutput;
import com.docusign.report.domain.ManageDataAPI;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.ReportData;
import com.docusign.report.domain.TableColumnMetaData;
import com.docusign.report.utils.DateTimeUtil;
import com.docusign.report.utils.LambdaUtilities;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportJDBCService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Value("${app.db.auditorname}")
	private String auditorName;

	@Transactional
	public void createTables(String[] ddlQueries) {

		int[] batchUpdateResponse = jdbcTemplate.batchUpdate(ddlQueries);

		Arrays.asList(batchUpdateResponse)
				.forEach(response -> log.info(" :::::::::::: response :::::::::::: " + response));
	}

	@Transactional(readOnly = true)
	public TableColumnMetaData getTableColumns(String tableName) {

		SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " where 0=1");

		int columnCount = sqlRowSet.getMetaData().getColumnCount();

		Map<String, String> columnNameTypeMap = new HashMap<String, String>(columnCount);
		Map<String, Integer> columnNameIndexMap = new HashMap<String, Integer>(columnCount);

		StringBuilder columnNames = new StringBuilder();
		columnNames.append("(");

		StringBuilder positionPlaceHolders = new StringBuilder();
		positionPlaceHolders.append("(");

		StringBuilder namedPlaceHolders = new StringBuilder();
		namedPlaceHolders.append("(");

		for (int i = 1; i <= columnCount; i++) {

			String columnName = sqlRowSet.getMetaData().getColumnName(i).toLowerCase();

			log.debug("ColumnName " + columnName + " ColumnTypeName " + sqlRowSet.getMetaData().getColumnTypeName(i));

			columnNameTypeMap.put(columnName, sqlRowSet.getMetaData().getColumnTypeName(i));

			columnNameIndexMap.put(columnName, i);

			columnNames.append("`");
			columnNames.append(columnName);
			columnNames.append("`");

			columnNames.append(",");

			positionPlaceHolders.append("?");
			positionPlaceHolders.append(",");

			namedPlaceHolders.append(":" + columnName);
			namedPlaceHolders.append(",");

		}

		columnNames.deleteCharAt(columnNames.length() - 1);
		columnNames.append(")");

		positionPlaceHolders.deleteCharAt(positionPlaceHolders.length() - 1);
		positionPlaceHolders.append(")");

		namedPlaceHolders.deleteCharAt(namedPlaceHolders.length() - 1);
		namedPlaceHolders.append(")");

		StringBuilder insertSqlBuilder = new StringBuilder();

		insertSqlBuilder.append("insert into " + tableName + " ");
		insertSqlBuilder.append(columnNames.toString());
		insertSqlBuilder.append(" values ");
		insertSqlBuilder.append(positionPlaceHolders.toString());

		StringBuilder insertNamedSqlBuilder = new StringBuilder();

		insertNamedSqlBuilder.append("insert into " + tableName + " ");
		insertNamedSqlBuilder.append(" values ");
		insertNamedSqlBuilder.append(namedPlaceHolders.toString());

		log.debug("InsertQuery::::::::: {}", insertSqlBuilder.toString());
		log.debug("columnNameTypeMap::::::::: {}", columnNameTypeMap);
		log.debug("columnNameIndexMap::::::::: {}", columnNameIndexMap);

		return createTableColumnMetaData(tableName, columnNameTypeMap, columnNameIndexMap, insertSqlBuilder,
				insertNamedSqlBuilder);

	}

	private TableColumnMetaData createTableColumnMetaData(String tableName, Map<String, String> columnNameTypeMap,
			Map<String, Integer> columnNameIndexMap, StringBuilder insertSqlBuilder,
			StringBuilder insertnamedSqlBuilder) {

		TableColumnMetaData tableColumnMetaData = new TableColumnMetaData();

		tableColumnMetaData.setTableName(tableName);
		tableColumnMetaData.setColumnNameTypeMap(columnNameTypeMap);
		tableColumnMetaData.setColumnNameIndexMap(columnNameIndexMap);
		tableColumnMetaData.setInsertQuery(insertSqlBuilder.toString());
		tableColumnMetaData.setInsertNamedQuery(insertnamedSqlBuilder.toString());

		return tableColumnMetaData;
	}

	@Transactional
	public void deleteReportData(Set<String> accoundIdList, String batchId, String tableName) {

		List<Object[]> batchArgs = new ArrayList<Object[]>();

		accoundIdList.forEach(accountId -> {

			batchArgs.add(new Object[] { accountId, batchId });

		});
		jdbcTemplate.batchUpdate("DELETE FROM " + tableName + " WHERE accountid = ? and batchid = ?", batchArgs);
	}

	private List<Map<String, Object>> convertListToMap(List<List<ReportData>> reportRowsList, String accountId,
			String batchId, String processId) {

		List<Map<String, Object>> rowDataMapList = new ArrayList<>(reportRowsList.size());
		for (List<ReportData> reportDataList : reportRowsList) {

			Map<String, Object> columnDataMap = new HashMap<String, Object>();

			columnDataMap.put("recordid", UUID.randomUUID().toString());
			columnDataMap.put("createddatetime", DateTimeUtil.currentEpochTime());
			columnDataMap.put("createdby", auditorName);
			columnDataMap.put("accountid", accountId);
			columnDataMap.put("batchid", batchId);
			columnDataMap.put("processid", processId);

			reportDataList.forEach(reportData -> {

				columnDataMap.put(reportData.getReportColumnName(), reportData.getReportColumnValue());
			});

			rowDataMapList.add(columnDataMap);
		}

		return rowDataMapList;
	}

	@Transactional
	public String saveReportData(List<List<ReportData>> reportRowsList, TableColumnMetaData tableColumnMetaData,
			String accountId, String batchId, String processId, String nextUri, String primaryIdColumnName) {

		log.info(
				"Saving ReportData in tableName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
				tableColumnMetaData.getTableName(), reportRowsList.size(), accountId, batchId, processId, nextUri);

		String primaryIds = null;
		try {

			List<Map<String, Object>> rowDataMapList = convertListToMap(reportRowsList, accountId, batchId, processId);

			if (!StringUtils.isEmpty(primaryIdColumnName)) {

				primaryIds = findAndCollectPrimaryIds(rowDataMapList, primaryIdColumnName);

				if (!StringUtils.isEmpty(primaryIds)) {
					log.info("primaryIds for accountId -> {}, batchId -> {}, processId -> {} is {}", accountId, batchId,
							processId, primaryIds);
				} else {

					log.error("primaryIds is null for accountId -> {}, batchId -> {}, processId -> {}", accountId,
							batchId, processId);
				}
			}

			SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(rowDataMapList);
			int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(tableColumnMetaData.getInsertNamedQuery(),
					batch);

			log.info("updateCounts in saveReportData is {}", updateCounts);

		} catch (Exception exp) {

			log.error(
					"Exception {} occurred in saving ReportData in tableName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
					exp, tableColumnMetaData.getTableName(), reportRowsList.size(), accountId, batchId, processId,
					nextUri);
			exp.printStackTrace();
			throw new ResourceNotSavedException("Report Data not saved in local database for accountId -> " + accountId
					+ " batchId -> " + batchId + " processId -> " + processId + " nextUri -> " + nextUri
					+ " with exception " + exp.getMessage());
		}

		return primaryIds;
	}

	private String findAndCollectPrimaryIds(List<Map<String, Object>> rowDataMapList, String primaryIdColumnName) {

		List<String> primaryIds = new ArrayList<String>();

		for (Map<String, Object> rowMap : rowDataMapList) {

			primaryIds.add(String.valueOf(rowMap.get(primaryIdColumnName)));
		}

		return String.join(AppConstants.COMMA_DELIMITER, primaryIds);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Map<String, Object>> readReportData(Map<String, String> columnNameHeaderMap,
			Map<String, String> originalColumnNameHeaderMap, Map<String, Object> inputParams,
			ManageDataAPI csvReportDataExport, Integer pageNumber, Integer paginationLimit) {

		log.info("Reading ReportData with queryParams is {}", inputParams);

		List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();

		String selectSql = csvReportDataExport.getSelectSql();
		selectSql = prepareSelectSql(csvReportDataExport, selectSql);

		List<Map<String, Object>> selectDataMapList = null;
		if (null != csvReportDataExport.getSqlParams() && !csvReportDataExport.getSqlParams().isEmpty()) {

			Map<String, Object> paramsMap = formatPathParam(inputParams, csvReportDataExport);

			// order by envelopeid asc OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
			if (selectSql.contains(AppConstants.SQL_QUERY_OFFSET)) {

				int offset = (paginationLimit * pageNumber) - paginationLimit;
				paramsMap.put(AppConstants.SQL_QUERY_OFFSET, offset);
			}

			if (selectSql.contains(AppConstants.SQL_QUERY_LIMIT)) {

				paramsMap.put(AppConstants.SQL_QUERY_LIMIT, paginationLimit);
			}

			if (log.isDebugEnabled()) {
				paramsMap.forEach((key, value) -> {

					log.debug("ParamName is {} and paramValue is {}", key, value);
				});
			}

			log.info("Calling DB for query {}", selectSql);
			selectDataMapList = namedParameterJdbcTemplate.queryForList(selectSql, paramsMap);

			if (log.isDebugEnabled()) {
				columnNameHeaderMap.forEach((key, value) -> {

					log.debug("ColumnName as Key {} and HeaderName as {}", key, value);

				});
			}
		} else {

			log.info("Calling DB for query without any params {}", selectSql);
			selectDataMapList = jdbcTemplate.queryForList(selectSql);
		}

		log.info("Total Fetched rows are {}", selectDataMapList.size());
		List<DecorateOutput> decorateOutputList = csvReportDataExport.getDecorateOutput();

		List<String> removeMultiValuedHeaderList = new ArrayList<String>();
		selectDataMapList.forEach(selectDataMap -> {

			Map<String, Object> csvDataMap = new HashMap<String, Object>();
			selectDataMap.forEach((key, value) -> {

				key = key.toLowerCase();
				if (log.isDebugEnabled()) {

					log.debug("Key {} and value is {}, headerName is {}", key, value, columnNameHeaderMap.get(key));
				}

				Object decoratedOutput = formatOutput(decorateOutputList, value, key);

				if (decoratedOutput instanceof List) {

					List<String> columnValueList = (List<String>) decoratedOutput;

					if (null != columnValueList && !columnValueList.isEmpty()) {

						for (int i = 0; i < columnValueList.size(); i++) {

							csvDataMap.put(originalColumnNameHeaderMap.get(key) + "_" + i, columnValueList.get(i));
							columnNameHeaderMap.put(originalColumnNameHeaderMap.get(key) + "_" + i,
									originalColumnNameHeaderMap.get(key) + "_" + i);

						}

						prepareRemoveMultiValuedHeaderList(columnNameHeaderMap, removeMultiValuedHeaderList, key);
					}

				} else if (decoratedOutput instanceof Map) {

					Map<String, String> keyValueMap = (Map<String, String>) decoratedOutput;

					if (null != keyValueMap && !keyValueMap.isEmpty()) {

						for (Map.Entry<String, String> mapEntry : keyValueMap.entrySet()) {

							csvDataMap.put(originalColumnNameHeaderMap.get(key) + "_" + mapEntry.getKey(),
									mapEntry.getValue());
							columnNameHeaderMap.put(originalColumnNameHeaderMap.get(key) + "_" + mapEntry.getKey(),
									originalColumnNameHeaderMap.get(key) + "_" + mapEntry.getKey());

						}

						prepareRemoveMultiValuedHeaderList(columnNameHeaderMap, removeMultiValuedHeaderList, key);
					}
				} else {

					csvDataMap.put(columnNameHeaderMap.get(key), decoratedOutput);
				}

			});

			rowList.add(csvDataMap);
		});

		// Remove unused header
		Iterator<Entry<String, String>> colHeaderMapIterator = columnNameHeaderMap.entrySet().iterator();
		while (colHeaderMapIterator.hasNext()) {

			Entry<String, String> mapEntry = colHeaderMapIterator.next();

			if (log.isDebugEnabled()) {

				log.debug("removeMultiValuedHeaderList is {}, mapKey is {}", removeMultiValuedHeaderList,
						mapEntry.getKey());
			}
			if (null != removeMultiValuedHeaderList && !removeMultiValuedHeaderList.isEmpty()
					&& removeMultiValuedHeaderList.contains(mapEntry.getKey())) {

				if (log.isDebugEnabled()) {

					log.debug("Removing Key {} and HeaderName as {}", mapEntry.getKey(), mapEntry.getValue());
				}
				colHeaderMapIterator.remove();
			}
		}

		return checkColumnsToRow(decorateOutputList, rowList);
	}

	private static List<Map<String, Object>> checkColumnsToRow(List<DecorateOutput> decorateOutputList,
			List<Map<String, Object>> rowList) {

		if (null != decorateOutputList && !decorateOutputList.isEmpty() && null != rowList && !rowList.isEmpty()) {

			List<DecorateOutput> decorateOutputFilterList = decorateOutputList.stream().filter(
					output -> DecorateOutputType.SPLITCOLUMNTOROW.toString().equalsIgnoreCase(output.getOutputType()))
					.collect(Collectors.toList());

			if (null != decorateOutputFilterList && !decorateOutputFilterList.isEmpty()) {

				for (DecorateOutput decorateOutput : decorateOutputFilterList) {

					log.info("For decorateOutput Col Name -> {}, rowList size before listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
					ListIterator<Map<String, Object>> rowListIterator = rowList.listIterator();
					while (rowListIterator.hasNext()) {

						Map<String, Object> rowColKeyValueMap = rowListIterator.next();

						rowColKeyValueMap.forEach((columnName, dbValue) -> {

							formatColumnToRowOutput(decorateOutputList, dbValue, columnName, rowListIterator,
									rowColKeyValueMap);
						});

					}

					log.info("For decorateOutput Col Name -> {}, rowList size after listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
				}

			}
		}

		return rowList;
	}

	private static List<Map<String, Object>> formatColumnToRowOutput(List<DecorateOutput> decorateOutputList,
			Object dbValue, String columnName, ListIterator<Map<String, Object>> rowListIterator,
			Map<String, Object> rowColKeyValueMap) {

		List<Map<String, Object>> newList = null;
		DecorateOutput decorateOutput = decorateOutputList.stream()
				.filter(output -> columnName.equalsIgnoreCase(output.getDbColumnName())).findAny().orElse(null);

		if (null != decorateOutput && null != dbValue && !StringUtils.isEmpty(decorateOutput.getOutputType())) {

			DecorateOutputType decorateOutputTypeEnum = EnumUtils.getEnum(DecorateOutputType.class,
					decorateOutput.getOutputType().toUpperCase());

			switch (decorateOutputTypeEnum) {

			case SPLITCOLUMNTOROW:

				String columnDelimiter = decorateOutput.getOutputDelimiter();

				if (StringUtils.isEmpty(columnDelimiter)) {

					columnDelimiter = AppConstants.COMMA_DELIMITER;
				}

				if (((String) dbValue).contains(columnDelimiter)) {

					Set<Entry<String, Object>> entries = rowColKeyValueMap.entrySet();
					HashMap<String, Object> shallowCopy = (HashMap<String, Object>) entries.stream()
							.collect(LambdaUtilities.toMapWithNullValues(Map.Entry::getKey, Map.Entry::getValue));

					List<String> dbValueList = Stream.of(((String) dbValue).split(columnDelimiter))
							.collect(Collectors.toList());

					rowListIterator.remove();// Removing old row which has columns

					for (String dbValueAfterSplit : dbValueList) {

						if (log.isDebugEnabled()) {

							log.debug("dbValue is {} and dbValueAfterSplit is {}", dbValue, dbValueAfterSplit);
						}
						shallowCopy.put(columnName, dbValueAfterSplit);
						rowListIterator.add(shallowCopy);// Add new rows with single value per column

						shallowCopy = (HashMap<String, Object>) shallowCopy.entrySet().stream()
								.collect(LambdaUtilities.toMapWithNullValues(Map.Entry::getKey, Map.Entry::getValue));
					}
				}

				break;

			default:
				log.warn(
						" ################################## Wrong OutputType -> {} as option in formatColumnToRowOutput ################################## ",
						decorateOutput.getOutputType());
			}
		}

		return newList;
	}

	private void prepareRemoveMultiValuedHeaderList(Map<String, String> columnNameHeaderMap,
			List<String> removeMultiValuedHeaderList, String key) {

		if (!removeMultiValuedHeaderList.contains(key)) {
			removeMultiValuedHeaderList.add(key);
		}

		if (!removeMultiValuedHeaderList.contains(columnNameHeaderMap.get(key))) {
			removeMultiValuedHeaderList.add(columnNameHeaderMap.get(key));
		}
	}

	private String prepareSelectSql(ManageDataAPI csvReportDataExport, String selectSql) {

		if (!StringUtils.isEmpty(csvReportDataExport.getWhereClause())) {

			selectSql = selectSql + " " + csvReportDataExport.getWhereClause();
		}

		if (!StringUtils.isEmpty(csvReportDataExport.getOrderByClause())) {

			selectSql = selectSql + " " + csvReportDataExport.getOrderByClause();
		}
		return selectSql;
	}

	private Map<String, Object> formatPathParam(Map<String, Object> inputParams, ManageDataAPI csvReportDataExport) {

		List<PathParam> sqlParamList = csvReportDataExport.getSqlParams();

		Map<String, Object> paramsMap = new HashMap<String, Object>(sqlParamList.size());

		for (PathParam pathParam : sqlParamList) {

			Object paramValue = null;
			if ("DateEpoch".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("DateTimeEpoch".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("Date".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToISOLocalDate((String) inputParams.get(pathParam.getParamName()),
							pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToISOLocalDate((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("DateTime".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToISOLocalDateTime(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToISOLocalDateTime((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("SqlDate".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
							DateTimeUtil.convertToEpochTimeFromDateAsString(
									(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern()),
							0, ZoneOffset.UTC));

				} else {

					paramValue = Timestamp
							.valueOf(
									LocalDateTime.ofEpochSecond(
											DateTimeUtil.convertToEpochTimeFromDateAsString(
													(String) pathParam.getParamValue(), pathParam.getParamPattern()),
											0, ZoneOffset.UTC));
				}

			} else if ("SqlDateTime".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
							DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
									(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern()),
							0, ZoneOffset.UTC));

				} else {

					paramValue = Timestamp
							.valueOf(LocalDateTime.ofEpochSecond(
									DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
											(String) pathParam.getParamValue(), pathParam.getParamPattern()),
									0, ZoneOffset.UTC));

				}

			} else {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = inputParams.get(pathParam.getParamName());
				} else {

					paramValue = pathParam.getParamValue();
				}
			}

			paramsMap.put(pathParam.getParamName(), paramValue);
		}
		return paramsMap;
	}

	private Object formatOutput(List<DecorateOutput> decorateOutputList, Object dbValue, String columnName) {

		if (null != decorateOutputList && !decorateOutputList.isEmpty()) {

			DecorateOutput decorateOutput = decorateOutputList.stream()
					.filter(output -> columnName.equalsIgnoreCase(output.getDbColumnName())).findAny().orElse(null);

			if (null != decorateOutput && null != dbValue && !StringUtils.isEmpty(decorateOutput.getOutputType())) {

				DecorateOutputType decorateOutputTypeEnum = EnumUtils.getEnum(DecorateOutputType.class,
						decorateOutput.getOutputType().toUpperCase());

				switch (decorateOutputTypeEnum) {

				case DATEASEPOCHTIME:
					return DateTimeUtil.convertToLocalDateFromEpochTimeInSecs(Long.valueOf(dbValue + ""),
							decorateOutput.getOutputDateZone(), decorateOutput.getOutputDatePattern());
				case DATETIMEASEPOCHTIME:
					return DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(Long.valueOf(dbValue + ""),
							decorateOutput.getOutputDateZone(), decorateOutput.getOutputDatePattern());

				case DATE:

					String inputDatePattern = decorateOutput.getInputDatePattern();

					if (StringUtils.isEmpty(inputDatePattern)) {

						inputDatePattern = "yyyy-MM-dd";
					}

					LocalDate ldate = LocalDate.parse((String) dbValue, DateTimeFormatter.ofPattern(inputDatePattern));
					return DateTimeFormatter.ofPattern(decorateOutput.getOutputDatePattern()).format(ldate);

				case DATETIME:

					String inputDateTimePattern = decorateOutput.getInputDatePattern();

					if (StringUtils.isEmpty(inputDateTimePattern)) {

						inputDateTimePattern = "yyyy-MM-dd HH:mm:ss";
					}

					LocalDateTime ldateTime = LocalDateTime.parse((String) dbValue,
							DateTimeFormatter.ofPattern(inputDateTimePattern));
					if (!StringUtils.isEmpty(decorateOutput.getOutputDateZone())) {

						ldateTime.atZone(TimeZone.getTimeZone(decorateOutput.getOutputDateZone()).toZoneId());
					}

					return DateTimeFormatter.ofPattern(decorateOutput.getOutputDatePattern()).format(ldateTime);
				case ARRAY:

					List<String> columnValueList = null;
					String delimiter = decorateOutput.getOutputDelimiter();
					if (!StringUtils.isEmpty(delimiter)) {

						columnValueList = Stream.of(((String) dbValue).split(delimiter)).collect(Collectors.toList());
					}

					return columnValueList;
				case ARRAYMAP:

					Map<String, String> keyValueMap = new HashMap<String, String>();
					String outputDelimiter = decorateOutput.getOutputDelimiter();
					String keyValueDelimiter = decorateOutput.getKeyValueDelimiter();
					if (!StringUtils.isEmpty(outputDelimiter) && !StringUtils.isEmpty(keyValueDelimiter)) {

						List<String> keyValueList = Stream.of(((String) dbValue).split(outputDelimiter))
								.collect(Collectors.toList());
						for (String keyValue : keyValueList) {

							String key = null;
							String value = null;
							String[] keyValueSplitArr = keyValue.split(keyValueDelimiter);
							if (null != keyValueSplitArr && keyValueSplitArr.length == 2) {
								key = keyValueSplitArr[0];
								value = keyValueSplitArr[1];

							} else {
								key = keyValueSplitArr[0];
							}

							keyValueMap.put(key, value);
						}
					}

					return keyValueMap;
				default:
					log.warn(
							" ################################## Wrong OutputType -> {} as option in formatOutput ################################## ",
							decorateOutput.getOutputType());
				}
			}
		}

		return dbValue;
	}

	public List<Map<String, Object>> runSelectQuery(String selectSql) {

		log.info("Calling DB for select query {}", selectSql);
		List<Map<String, Object>> selectDataMapList = jdbcTemplate.queryForList(selectSql);

		log.info("Total Fetched rows are {}", selectDataMapList.size());

		return selectDataMapList;
	}

	public void runNonSelectQuery(String selectSql) {

		log.info("Calling DB for non-select query {}", selectSql);
		jdbcTemplate.batchUpdate(selectSql);
	}

	@Transactional
	public List<Map<String, Object>> readEnvelopeDataForDownload(ManageDataAPI csvReportDataExport,
			Map<String, Object> inputParams, Integer pageNumber, Integer paginationLimit) {

		log.info("Reading readEnvelopeDataForDownload with queryParams is {}", inputParams);

		String selectSql = "select * from " + csvReportDataExport.getTableName();

		selectSql = prepareSelectSql(csvReportDataExport, selectSql);

		List<Map<String, Object>> selectDataMapList = null;
		if (null != csvReportDataExport.getSqlParams() && !csvReportDataExport.getSqlParams().isEmpty()) {

			Map<String, Object> paramsMap = formatPathParam(inputParams, csvReportDataExport);

			// order by envelopeid asc OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
			if (selectSql.contains(AppConstants.SQL_QUERY_OFFSET)) {

				int offset = (paginationLimit * pageNumber) - paginationLimit;
				paramsMap.put(AppConstants.SQL_QUERY_OFFSET, offset);
			}

			if (selectSql.contains(AppConstants.SQL_QUERY_LIMIT)) {

				paramsMap.put(AppConstants.SQL_QUERY_LIMIT, paginationLimit);
			}

			if (log.isDebugEnabled()) {
				paramsMap.forEach((key, value) -> {

					log.debug("ParamName is {} and paramValue is {}", key, value);
				});
			}

			try {

				log.info("Calling DB for query {}", selectSql);
				selectDataMapList = namedParameterJdbcTemplate.queryForList(selectSql, paramsMap);
			} catch (ArrayIndexOutOfBoundsException exp) {

				log.error("Placeholder mismatch between sql and sql params list, verify sql and/or sql params list");
				exp.printStackTrace();
			}

		} else {

			log.info("Calling DB in readEnvelopeDataForDownload for query without any params {}", selectSql);
			selectDataMapList = jdbcTemplate.queryForList(selectSql);
		}

		log.info("Total Fetched rows are {}", selectDataMapList.size());

		return selectDataMapList;
	}

}