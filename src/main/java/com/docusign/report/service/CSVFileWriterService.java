package com.docusign.report.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.FailureCode;
import com.docusign.report.common.constant.FailureStep;
import com.docusign.report.common.exception.AsyncInterruptedException;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.common.exception.ResourceConditionFailedException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.db.controller.CoreCacheDataLogController;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.domain.DownloadDocs;
import com.docusign.report.domain.ManageDataAPI;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.TableColumnMetaData;
import com.docusign.report.utils.FileUtil;
import com.docusign.report.utils.ReportAppUtil;
import com.docusign.report.validator.CSVFileWriterValidator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CSVFileWriterService {

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private FileWriterService fileWriterService;

	@Autowired
	private ScriptEngineManager scriptEngineManager;

	@Autowired
	private CSVFileWriterValidator csvFileWriterValidator;

	@Autowired
	private CoreCacheDataLogController coreCacheDataLogController;

	@Value("${app.authorization.userId}")
	private String authUserId;

	@Value("${app.totalRowsPerProcess}")
	private Integer totalRowsPerProcess;

	public Map<String, String> createHeaderList(TableColumnMetaData tableColumnMetaData, String selectSql) {

		log.info("Creating headers for the CSV for selectSql -> {}", selectSql);

		List<String> selectColumnList = extractSelectColumnList(tableColumnMetaData, selectSql);

		log.info("selectColumnList is {}", selectColumnList);
		if (null == selectColumnList || selectColumnList.isEmpty()) {

			throw new ResourceConditionFailedException(
					"Columns list cannot be empty or null, please check the select query");
		}

		Map<String, String> columnNameHeaderMap = new LinkedHashMap<String, String>();

		selectColumnList.forEach(column -> {

			try {

				String keyValue = coreCacheDataLogController.findByCacheKey(column).getBody().getCacheValue();
				columnNameHeaderMap.put(column, keyValue);

			} catch (ResourceNotFoundException exp) {

				log.warn("No cache value exists for key (columnName) -> {}", column);

				try {
					String keyValue = coreCacheDataLogController.findByCacheKey(column.toUpperCase()).getBody()
							.getCacheValue();
					columnNameHeaderMap.put(column.toLowerCase(), keyValue);
				} catch (ResourceNotFoundException upperExp) {

					log.warn("No cache value exists for key (columnName) with uppercase -> {}", column);
				}
			}

		});

		return columnNameHeaderMap;

	}

	private List<String> extractSelectColumnList(TableColumnMetaData tableColumnMetaData, String selectSql) {

		selectSql = selectSql.trim().replaceAll("\\s{2,}", " ").toLowerCase();
		if (selectSql.indexOf("select ") == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after select");
		}

		if (selectSql.indexOf(" from " + tableColumnMetaData.getTableName()) == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after before and after from");
		}

		String[] selectColumns = selectSql.split("select ");
		String sqlColumns = selectColumns[1].split(" from " + tableColumnMetaData.getTableName())[0];

		return Stream.of(sqlColumns.split(",")).map(String::trim).collect(Collectors.toList());
	}

	public void writeCSV(List<Map<String, Object>> rowList, Collection<String> headerList, String csvPath,
			boolean writeToStream, HttpServletResponse response) throws IOException {

		String[] headerArray = headerList.stream().toArray(n -> new String[n]);

		ICsvMapWriter mapWriter = null;
		try {

			if (writeToStream && null != response) {

				mapWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			} else {

				mapWriter = new CsvMapWriter(new FileWriter(csvPath), CsvPreference.STANDARD_PREFERENCE);
			}

			// write the header
			mapWriter.writeHeader(headerArray);

			// write the customer maps
			for (Map<String, Object> row : rowList) {

				log.debug("Row data is -> {}", row);
				mapWriter.write(row, headerArray);
			}

		} catch (IOException exp) {

			exp.printStackTrace();
			throw exp;
		} finally {

			if (writeToStream) {

				log.info("All data written to stream");
			} else {
				log.info("All data written to a csv -> {}", csvPath);
			}
			if (null != mapWriter) {

				try {

					mapWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void writeEnvelopeDocumentsToDisk(List<Map<String, Object>> envelopeDataList, String downloadFolderPath,
			DownloadDocs downloadDocs, String batchId, ManageDataAPI csvReportDataExport, String batchType)
			throws IOException {

		Path parentDirectory = Paths.get(downloadFolderPath);
		log.info("ParentDirectory in writeEnvelopeDocuments is {}", parentDirectory.toString());

		List<PathParam> downloadParams = downloadDocs.getDownloadParams();

		csvFileWriterValidator.validateDownloadDocs(downloadDocs, downloadParams);

		String fileSaveFormat = ReportAppUtil.findPathParam(downloadParams, AppConstants.FILE_SAVE_FORMAT)
				.getParamValue();

		APICategoryType apiCategoryType = ReportAppUtil
				.getAPICategoryType(downloadDocs.getAssociatedData().getApiCategory());

		if (apiCategoryType == APICategoryType.ESIGNAPI) {

			csvFileWriterValidator.validateFileSaveFormatEnum(fileSaveFormat);

			csvFileWriterValidator.validateFileSaveFormatWithAPIUri(fileSaveFormat,
					downloadDocs.getAssociatedData().getApiUri());
		}
		processReportEnvelopeDataToWriteFiles(envelopeDataList, downloadDocs, batchId, parentDirectory, downloadParams,
				fileSaveFormat, csvReportDataExport, batchType);

	}

	private void processReportEnvelopeDataToWriteFiles(List<Map<String, Object>> envelopeDataList,
			DownloadDocs downloadDocs, String batchId, Path parentDirectory, List<PathParam> downloadParams,
			String fileSaveFormat, ManageDataAPI csvReportDataExport, String batchType) {

		ScriptEngine engine = scriptEngineManager.getEngineByName(AppConstants.SCRIPT_ENGINE_NAME);

		String downloadFileName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FILE_NAME);
		String downloadFolderName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FOLDER_NAME);

		log.info("downloadFileName is {}, downloadFolderName is {} and downloadParams are {}", downloadFileName,
				downloadFolderName, downloadParams);

		// Below code partitions all envelopeDataList into chunks with each chunk having
		// totalRowsPerProcess records
		// Below code to test first
		final AtomicReference<String> groupbyString = new AtomicReference<String>();
		List<CompletableFuture<ConcurrentProcessLogDefinition>> reportEnvelopeDataList = new ArrayList<CompletableFuture<ConcurrentProcessLogDefinition>>();

		if (!StringUtils.isEmpty(csvReportDataExport.getGroupByColumn())) {

			groupbyString.set(csvReportDataExport.getGroupByColumn());
		} else if (!StringUtils.isEmpty(csvReportDataExport.getOrderByClause())) {

			String orderByClause = csvReportDataExport.getOrderByClause().toLowerCase();
			groupbyString.set(orderByClause.split(" ")[2].split(",")[0].trim());
		}

		if (!StringUtils.isEmpty(groupbyString.get())) {

			log.info(
					"About to group the data set by {}, it might take some time based on the records return from the DB",
					groupbyString.get());
			Collection<List<Map<String, Object>>> groupByString = envelopeDataList.stream()
					.collect(Collectors.groupingBy(it -> it.get(groupbyString.get()))).values();

			log.info("groupByString size is {}", groupByString.size());

			partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(downloadDocs, batchId, parentDirectory,
					downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName,
					reportEnvelopeDataList, groupByString, batchType);

		} else {// Group first by accountid, then create subset of each grouped data by
				// totalRowsPerProcess

			log.info(
					"About to group the data set by accountid, it might take some time based on the records return from the DB");
			Collection<List<Map<String, Object>>> groupByAccountId = envelopeDataList.stream()
					.collect(Collectors.groupingBy(it -> it.get("accountid"))).values();

			log.info("groupByAccountId size is {}", groupByAccountId.size());

			partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(downloadDocs, batchId, parentDirectory,
					downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName,
					reportEnvelopeDataList, groupByAccountId, batchType);

		}

		log.info("Size of reportDataFutureAccountList is {}", reportEnvelopeDataList.size());
		try {

			CompletableFuture
					.allOf(reportEnvelopeDataList.toArray(new CompletableFuture[reportEnvelopeDataList.size()])).get();
		} catch (InterruptedException exp) {

			log.error(
					"InterruptedException {} occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId {}",
					exp, batchId);

			exp.printStackTrace();
			throw new AsyncInterruptedException("InterruptedException " + exp
					+ " occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId " + batchId
					+ " message " + exp.getMessage());
		} catch (ExecutionException exp) {

			log.error(
					"ExecutionException {} occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId {} and the cause is {}",
					exp, batchId, exp.getCause());
			exp.printStackTrace();

			throw new AsyncInterruptedException("ExecutionException " + exp
					+ " occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId " + batchId
					+ " message " + exp.getMessage() + " cause is " + exp.getCause());
		}
	}

	private void partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(DownloadDocs downloadDocs, String batchId,
			Path parentDirectory, List<PathParam> downloadParams, String fileSaveFormat, ScriptEngine engine,
			String downloadFileName, String downloadFolderName,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportEnvelopeDataList,
			Collection<List<Map<String, Object>>> groupByAccountId, String batchType) {

		log.info(
				"About to partition each group by totalRowsPerProcess -> {}, it might take some time based on the record return from the DB",
				totalRowsPerProcess);

		groupByAccountId.forEach(group -> {

			final AtomicInteger groupByCounter = new AtomicInteger(0);
			Collection<List<Map<String, Object>>> groupByTotalRows = group.stream()
					.collect(Collectors.groupingBy(it -> groupByCounter.getAndIncrement() / totalRowsPerProcess))
					.values();

			log.info("groupByTotalRows size is {}", groupByTotalRows.size());
			log.debug("groupByTotalRows -> {}", groupByTotalRows);

			groupByTotalRows.forEach(partitionedSet -> {

				reportEnvelopeDataList.add(processRowDataAsync(partitionedSet, downloadDocs, batchId, parentDirectory,
						downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName, batchType));

			});

		});
	}

	private String extractPathParamValue(List<PathParam> downloadParams, String paramName) {

		PathParam pathParam = ReportAppUtil.findPathParam(downloadParams, paramName);
		if (null != pathParam) {
			return pathParam.getParamValue();
		}

		return null;
	}

	private CompletableFuture<ConcurrentProcessLogDefinition> processRowDataAsync(
			List<Map<String, Object>> envelopeDataList, DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			List<PathParam> downloadParams, String fileSaveFormat, ScriptEngine engine, String downloadFileName,
			String downloadFolderName, String batchType) {

		return CompletableFuture.supplyAsync((Supplier<ConcurrentProcessLogDefinition>) () -> {

			log.info("accountId -> {}, totalListSize -> {}", (String) envelopeDataList.get(0).get("accountid"),
					envelopeDataList.size());
			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(
					Long.valueOf(envelopeDataList.size()), batchId, batchType,
					(String) envelopeDataList.get(0).get("accountid"), authUserId);

			for (Map<String, Object> envelopeDataMap : envelopeDataList) {

				try {

					String fileName = null;
					String folderName = null;
					Map<String, Object> inputParams = new HashMap<String, Object>();

					FileUtil.populateInputParams(envelopeDataMap, inputParams, downloadDocs);

					fileName = FileUtil.evaluateFileName(downloadParams, engine, downloadFileName, envelopeDataMap,
							fileName);

					folderName = FileUtil.evaluateFileFolderName(downloadParams, engine, downloadFolderName,
							envelopeDataMap, folderName);

					fileWriterService.pullDocumentAndWriteToDirectory(downloadDocs, batchId, parentDirectory,
							fileSaveFormat, fileName, folderName, inputParams,
							concurrentProcessLogDefinition.getProcessId());

				} catch (Exception exp) {

					exp.printStackTrace();
					log.error("Exception {} occurred with message {} for envelopeId {} and accountId {}",
							exp.getCause(), exp.getMessage(), envelopeDataMap.get("envelopeid"),
							envelopeDataMap.get("accountid"));

					batchDataService.createFailureRecord(
							envelopeDataMap.get("accountid") + "_" + envelopeDataMap.get("envelopeid"), batchId,
							FailureCode.ERROR_107.toString(), exp.getMessage(),
							FailureStep.PROCESSROWDATAFORASYNC.toString(), exp);
				}
			}

			return concurrentProcessLogDefinition;
		}, recordTaskExecutor).handle((concurrentProcessLogDefinition, exp) -> {

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside csvFileWriteService.handleAsync and exp is {} for batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, batchId);

				exp.printStackTrace();

				batchDataService.createFailureRecord((String) envelopeDataList.get(0).get("accountid"), batchId,
						FailureCode.ERROR_107.toString(), exp.getMessage(), FailureStep.PROCESSROWDATAASYNC.toString(),
						exp);

			} else {

				batchDataService.finishConcurrentProcess(concurrentProcessLogDefinition.getProcessId());
			}
			return concurrentProcessLogDefinition;
		});

	}

	public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {

			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class FunctionDefinition {

		private static final String FN = "fn:";
		private static final String FUNCTION = "function:";
		String functionValue = null;
		String functionExpression = null;
		boolean functionAvailable = false;

		private String extractFunctionName(String downloadFunctionName) {

			String functionName = null;

			if (downloadFunctionName.contains(FN)) {

				functionName = downloadFunctionName.split(":")[1];
			} else if (downloadFunctionName.contains(FUNCTION)) {

				functionName = downloadFunctionName.split(":")[1];
			}

			log.info("functionName in FunctionDefinition is {}", functionName);
			return functionName;
		}

		private void validateFunctionExpression(String functionExpression) {

			if (StringUtils.isEmpty(functionExpression)) {

				throw new InvalidInputException(
						"functionExpression cannot be null, kindly check the paramName it should be in format {functionName}_Definition");
			}
		}

		public void createFunctionDefinition(String downloadFunctionName, List<PathParam> downloadParams,
				ScriptEngine engine, Map<String, Object> envelopeDataMap) {

			String functionName = extractFunctionName(downloadFunctionName);

			if (!StringUtils.isEmpty(functionName)) {

				functionAvailable = true;
				functionExpression = ReportAppUtil.findPathParam(downloadParams, functionName + "_Definition")
						.getParamValue();
				validateFunctionExpression(functionExpression);

				String functionParams = ReportAppUtil.findPathParam(downloadParams, functionName + "_Params")
						.getParamValue();

				List<String> functionParamList = null;

				if (!StringUtils.isEmpty(functionParams)) {
					if (functionParams.contains(",")) {

						functionParamList = Stream.of(functionParams.split(",")).map(String::trim)
								.collect(Collectors.toList());
					} else {

						functionParamList = new ArrayList<String>();
						functionParamList.add(functionParams);

					}
				}

				evaluateValueFunctionExpression(downloadParams, engine, envelopeDataMap, functionName,
						functionParamList);
			}

		}

		private void evaluateValueFunctionExpression(List<PathParam> downloadParams, ScriptEngine engine,
				Map<String, Object> envelopeDataMap, String functionName, List<String> functionParamList) {

			try {

				engine.eval(functionExpression);
				Invocable invocable = (Invocable) engine;

				if (null != functionParamList && !functionParamList.isEmpty()) {

					List<Object> objectFunctionParamList = new ArrayList<Object>(functionParamList.size());

					for (String functionParam : functionParamList) {

						// read data from database
						objectFunctionParamList.add(envelopeDataMap.get(functionParam));
					}

					Object[] objectFunctionParamArr = new Object[objectFunctionParamList.size()];
					functionValue = (String) invocable.invokeFunction(functionName,
							objectFunctionParamList.toArray(objectFunctionParamArr));
				} else {
					functionValue = (String) invocable.invokeFunction(functionName);
				}

			} catch (NoSuchMethodException exp) {

				log.error(
						"NoSuchMethodException thrown in FunctionDefinition.createFunctionDefinition for functionName {}, downloadParams -> {}",
						functionName, downloadParams);
				exp.printStackTrace();
			} catch (ScriptException exp) {

				log.error(
						"ScriptException thrown in FunctionDefinition.createFunctionDefinition for functionName {}, downloadParams -> {}",
						functionName, downloadParams);
				exp.printStackTrace();
			}
		}
	}

}