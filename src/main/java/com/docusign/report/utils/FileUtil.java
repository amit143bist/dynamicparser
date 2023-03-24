package com.docusign.report.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.domain.DownloadDocs;
import com.docusign.report.domain.PathParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

	public static void populateInputParams(Map<String, Object> envelopeDataMap, Map<String, Object> inputParams,
			DownloadDocs downloadDocs) {

		String apiCategory = downloadDocs.getAssociatedData().getApiCategory();

		APICategoryType apiCategoryType = ReportAppUtil.getAPICategoryType(apiCategory);

		if (apiCategoryType == APICategoryType.ESIGNAPI) {

			PathParam accountIdParam = ReportAppUtil.findPathParam(downloadDocs.getDownloadParams(),
					AppConstants.ACCOUNT_ID_COL_NAME);

			PathParam envIdParam = ReportAppUtil.findPathParam(downloadDocs.getDownloadParams(),
					AppConstants.ENVELOPE_ID_COL_NAME);

			if (null == accountIdParam || StringUtils.isEmpty(accountIdParam.getParamValue())) {

				throw new ResourceNotFoundException(
						"accountid param or value not configured in ruleEngine for paramKey "
								+ AppConstants.ACCOUNT_ID_COL_NAME);
			}

			if (null == envIdParam || StringUtils.isEmpty(envIdParam.getParamValue())) {

				throw new ResourceNotFoundException(
						"envelopeid param or value not configured in ruleEngine for paramKey "
								+ AppConstants.ENVELOPE_ID_COL_NAME);
			}

			if (null != envelopeDataMap.get(accountIdParam.getParamValue())) {

				inputParams.put("inputAccountId", envelopeDataMap.get(accountIdParam.getParamValue()));
			} else {

				throw new ResourceNotFoundException("accountid cannot be null in report database");
			}

			if (null != envelopeDataMap.get(envIdParam.getParamValue())) {

				inputParams.put("inputEnvelopeId", envelopeDataMap.get(envIdParam.getParamValue()));
			} else {

				throw new ResourceNotFoundException("envelopeid cannot be null in report database");
			}
		} else {

			PathParam docDownloadParam = ReportAppUtil.findPathParam(downloadDocs.getDownloadParams(),
					AppConstants.DOC_DOWNLOAD_COLUMN_LABELS);

			if (null != docDownloadParam) {

				List<String> docDownloadParamList = Stream
						.of(docDownloadParam.getParamValue().split(AppConstants.COMMA_DELIMITER)).map(String::trim)
						.collect(Collectors.toList());

				if (null != docDownloadParamList && !docDownloadParamList.isEmpty()) {

					docDownloadParamList.forEach(docParam -> {

						inputParams.put(docParam, envelopeDataMap.get(docParam));

					});
				}
			}

		}

	}

	// Below method to evaluate File/Folder Name
	public static String evaluateFileFolderName(List<PathParam> downloadParams, ScriptEngine engine,
			String downloadFileFolderName, Map<String, Object> envelopeDataMap, String fileFolderName) {

		FunctionDefinition fileFolderNameFunctionDefinition = new FunctionDefinition();

		if (!StringUtils.isEmpty(downloadFileFolderName)) {// Evaluate FileFolderName from function expression

			fileFolderNameFunctionDefinition.createFunctionDefinition(downloadFileFolderName, downloadParams, engine,
					envelopeDataMap);
			fileFolderName = fileFolderNameFunctionDefinition.getFunctionValue();
		}

		if (null == fileFolderName && !fileFolderNameFunctionDefinition.isFunctionAvailable()
				&& null != downloadFileFolderName && null != envelopeDataMap.get(downloadFileFolderName)) {// Evaluate
																											// FileFolderName
																											// from db
																											// column

			fileFolderName = (String) envelopeDataMap.get(downloadFileFolderName);
		}

		if (null == fileFolderName && null != downloadFileFolderName) {

			fileFolderName = downloadFileFolderName;
		}

		return fileFolderName;
	}

	public static String evaluateFileName(List<PathParam> downloadParams, ScriptEngine engine, String downloadFileName,
			Map<String, Object> envelopeDataMap, String fileName) {

		fileName = evaluateFileFolderName(downloadParams, engine, downloadFileName, envelopeDataMap, fileName);

		if (null == fileName) {// If fileName is not sent in csvgenerate.json then envelopeId will be the
								// fileName

			// read value from Database
			log.warn("Since fileName is not sent in csvgenerate.json so setting envelopeId as fileName");
			fileName = (String) envelopeDataMap.get("envelopeId");
		}
		return fileName;
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

			log.debug("functionName in FunctionDefinition is {}", functionName);
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
					if (functionParams.contains(AppConstants.COMMA_DELIMITER)) {

						functionParamList = Stream.of(functionParams.split(AppConstants.COMMA_DELIMITER))
								.map(String::trim).collect(Collectors.toList());
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
	
	public static void replaceCSVHeader(Collection<String> headerList, String csvPath, boolean writeToStream,
			HttpServletResponse response) throws IOException {

		String[] headerArray = headerList.stream().toArray(n -> new String[n]);
		ICsvMapWriter mapWriter = null;
		try {

			if (writeToStream && null != response) {

				mapWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			} else {

				mapWriter = new CsvMapWriter(new FileWriter(csvPath, true), CsvPreference.STANDARD_PREFERENCE);
			}

			// write the header
			mapWriter.writeHeader(headerArray);

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

	public static void writeCSVHeader(Collection<String> headerList, String csvPath, boolean writeToStream,
			HttpServletResponse response) throws IOException {

		String[] headerArray = headerList.stream().toArray(n -> new String[n]);
		ICsvMapWriter mapWriter = null;
		try {

			if (writeToStream && null != response) {

				mapWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			} else {

				mapWriter = new CsvMapWriter(new FileWriter(csvPath, true), CsvPreference.STANDARD_PREFERENCE);
			}

			// write the header
			mapWriter.writeHeader(headerArray);

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

	public static void writeAndAppendToCSV(List<Map<String, Object>> rowList, Collection<String> headerList,
			String csvPath, boolean writeToStream, HttpServletResponse response) throws IOException {

		String[] headerArray = headerList.stream().toArray(n -> new String[n]);

		ICsvMapWriter mapWriter = null;
		try {

			if (writeToStream && null != response) {

				mapWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			} else {

				mapWriter = new CsvMapWriter(new FileWriter(csvPath, true), CsvPreference.STANDARD_PREFERENCE);
			}

			// write the customer maps
			for (Map<String, Object> row : rowList) {

				if (log.isDebugEnabled()) {

					log.debug("Row data for csvPath is -> {}", row);
				}

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

	public static void writeCSV(List<Map<String, Object>> rowList, Collection<String> headerList, String csvPath,
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

				if (log.isDebugEnabled()) {

					log.debug("Row data for csvPath is -> {}", row);
				}
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
}