package com.docusign.report.shell.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.docusign.report.common.constant.JobType;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.db.controller.CoreDynamicTableController;
import com.docusign.report.domain.BatchTriggerInformation;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.TableCreationRequest;
import com.docusign.report.service.BatchTriggerService;
import com.docusign.report.shell.DocuSignShellCommandHelper;
import com.docusign.report.shell.DocuSignShellHelper;
import com.docusign.report.utils.DateTimeUtil;
import com.docusign.report.utils.ReportAppUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@ShellComponent
@Slf4j
public class ReportJobCommands {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	DocuSignShellHelper shellHelper;

	@Autowired
	BatchTriggerService batchTriggerService;

	@Autowired
	CoreDynamicTableController coreDynamicTableController;

	@Autowired
	DocuSignShellCommandHelper docuSignShellCommandHelper;

	@Value("${app.createTablesJsonFilePath}")
	String createTablesJsonFilePath;

	@ShellMethod(value = "TRIGGER JOB, validCommand is -> triggerjob batchStartDateTime $batchStartDateTime batchEndDateTime $batchEndDateTime numberOfHours $numberOfHours jobType $jobType refreshDataBase $refreshDataBase dynamicParams $dynamicParams", key = "triggerjob")
	public void triggerBatch(
			@ShellOption(value = "batchStartDateTime", defaultValue = "defaultValue") String batchStartDateTime,
			@ShellOption(value = "batchEndDateTime", defaultValue = "defaultValue") String batchEndDateTime,
			@ShellOption(value = "numberOfHours", defaultValue = "-1") Integer numberOfHours,
			@ShellOption(value = "jobType", defaultValue = "defaultValue") String jobType,
			@ShellOption(value = "refreshDataBase", defaultValue = "false") String refreshDataBase,
			@ShellOption(value = "dynamicParams", defaultValue = "defaultValue") String dynamicParams)
			throws IOException {

		log.info(
				" ^^^^^^^^^^^^^^^^^^^^ Inputs to triggerjob shell command are jobType -> {}, batchStartDateTime-> {} batchEndDateTime-> {} numberOfHours -> {} refreshDataBase -> {} and dynamicParams -> {} ^^^^^^^^^^^^^^^^^^^^ ",
				jobType, batchStartDateTime, batchEndDateTime, numberOfHours, refreshDataBase, dynamicParams);

		try {

			if (StringUtils.isEmpty(jobType)) {

				throw new InvalidInputException("jobType cannot be empty or null");
			}

			EnumUtils.isValidEnum(JobType.class, jobType);

			validateBatchParametersAndTriggerService(batchStartDateTime, batchEndDateTime, numberOfHours,
					prepareBatchTriggerInformation(refreshDataBase, dynamicParams), jobType,
					"triggerjob batchStartDateTime $batchStartDateTime batchEndDateTime $batchEndDateTime numberOfHours $numberOfHours jobType $jobType refreshDataBase $refreshDataBase dynamicParams $dynamicParams");
		} catch (Exception exp) {

			exp.printStackTrace();
		}
	}

	private BatchTriggerInformation prepareBatchTriggerInformation(String refreshDataBase, String dynamicParams)
			throws JsonMappingException, JsonProcessingException {

		BatchTriggerInformation batchTriggerInformation = null;

		if (!StringUtils.isEmpty(dynamicParams) && !"defaultValue".equalsIgnoreCase(dynamicParams)) {

			batchTriggerInformation = objectMapper.readValue(dynamicParams, BatchTriggerInformation.class);

			if (!StringUtils.isEmpty(batchTriggerInformation.getJobType())) {

				EnumUtils.isValidEnum(JobType.class, batchTriggerInformation.getJobType());
			}
		} else {

			batchTriggerInformation = new BatchTriggerInformation();
			batchTriggerInformation.setPathParams(new ArrayList<PathParam>());
		}

		List<PathParam> pathParamList = batchTriggerInformation.getPathParams();

		PathParam pathParam = new PathParam();
		pathParam.setParamName("refreshDataBase");
		pathParam.setParamValue(refreshDataBase);

		pathParamList.add(pathParam);

		batchTriggerInformation.setPathParams(pathParamList);
		return batchTriggerInformation;
	}

	@ShellMethod(value = "ON DEMAND - Fetch Batch data from User's input", key = { "batchops", "batchOps" })
	public void batchOps() {

		docuSignShellCommandHelper.captureBatchOperations();
	}

	@ShellMethod(value = "Need to run once only, Generate Envelope data the local database to csv, validCommand is -> generateTables", key = {
			"generateTables", "generatetables" })
	public void generateTables() throws IOException {

		log.info(" ^^^^^^^^^^^^^^^^^^^^ generateReportTables shell command is triggered ^^^^^^^^^^^^^^^^^^^^ ");

		File createTableFile = new File(createTablesJsonFilePath);
		TableCreationRequest tableCreationRequest = objectMapper.readValue(new FileReader(createTableFile),
				TableCreationRequest.class);

		coreDynamicTableController.createDynamicTables(tableCreationRequest);
	}

	@ShellMethod(value = "MergeFile Job, validCommand is -> mergefiles mergeFileNameFullPath $mergeFileNameFullPath inputFilesDir $inputFilesDir", key = "mergefiles")
	public void mergeFiles(
			@ShellOption(value = "mergeFileNameFullPath", defaultValue = "defaultValue") String mergeFileNameFullPath,
			@ShellOption(value = "inputFilesDir", defaultValue = "defaultValue") String inputFilesDir)
			throws IOException {

		String[] headers = null;
		String mergeFileHeaderLine = null;
		String firstFile = mergeFileNameFullPath;
		Scanner scanner = new Scanner(new File(firstFile));

		if (scanner.hasNextLine()) {

			mergeFileHeaderLine = scanner.nextLine().trim();
			headers = mergeFileHeaderLine.split(",");
		}

		scanner.close();

		File inputDirFile = new File(inputFilesDir);
		File[] listOfFilesToBeMerged = inputDirFile.listFiles();

		Arrays.sort(listOfFilesToBeMerged, Comparator.comparing(File::getName));

		Iterator<File> iterFiles = Arrays.asList(listOfFilesToBeMerged).iterator();
		BufferedWriter writer = new BufferedWriter(new FileWriter(firstFile, true));
		writer.newLine();

		while (iterFiles.hasNext()) {
			File nextFile = iterFiles.next();

			log.info("Filetobemerged is {} ", nextFile.getName());
			BufferedReader reader = new BufferedReader(new FileReader(nextFile));

			String line = null;

			String firstLineAsStr = null;
			String[] firstLine = null;
			if ((line = reader.readLine()) != null) {

				firstLineAsStr = line.trim();
				firstLine = firstLineAsStr.split(",");
			}

			if (!Arrays.equals(headers, firstLine)) {

				log.info("headers in the mergedFile is {}", mergeFileHeaderLine);
				log.info("firstLine in the inputfile is {}", firstLineAsStr);
				reader.close();
				throw new RuntimeException(
						"Header mis-match between CSV files: '" + firstFile + "' and '" + nextFile.getAbsolutePath());
			}

			while ((line = reader.readLine()) != null) {

				if (null != line && line.trim().length() > 0) {

					writer.write(line);
					writer.newLine();
				}

			}

			reader.close();
		}
		writer.close();
	}

	@ShellMethod(value = "SplitFile Job, validCommand is -> splitfiles startFileNameFullPath $startFileNameFullPath splitFilePrefix $splitFilePrefix totalLinesPerFile $totalLinesPerFile", key = "splitfiles")
	public void splitFiles(
			@ShellOption(value = "startFileNameFullPath", defaultValue = "defaultValue") String startFileNameFullPath,
			@ShellOption(value = "splitFilePrefix", defaultValue = "splitFile") String splitFilePrefix,
			@ShellOption(value = "totalLinesPerFile", defaultValue = "-1") Integer totalLinesPerFile)
			throws IOException {

		try {
			int count = -1;
			File file = new File(startFileNameFullPath);
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) { // counting the lines in the input file
				scanner.nextLine();
				count++;
			}
			scanner.close();
			log.info("Total Lines in the startFiles is {}", new Integer(count));
			int files = 0;
			if ((count % totalLinesPerFile) == 0) {
				files = (count / totalLinesPerFile);
			} else {
				files = (count / totalLinesPerFile) + 1;
			}
			log.info("TotalFile that will be created are {}", new Integer(files)); // number of files that shall be
																					// created

			ReportAppUtil.splitFiles(totalLinesPerFile, files, startFileNameFullPath, splitFilePrefix);
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@ShellMethod(value = "RemoveDuplicates Job, validCommand is -> removeduplicates sourceFileFullPath $sourceFileFullPath sourceFileColumnIndex $sourceFileColumnIndex mergedFileFullPath $mergedFileFullPath mergedFileColumnIndex $mergedFileColumnIndex newFileFullPath $newFileFullPath", key = "removeduplicates")
	public void removeDuplicatesFiles(
			@ShellOption(value = "sourceFileFullPath", defaultValue = "defaultValue") String sourceFileFullPath,
			@ShellOption(value = "mergedFileFullPath", defaultValue = "defaultValue") String mergedFileFullPath,
			@ShellOption(value = "sourceFileColumnIndex", defaultValue = "-1") String sourceFileColumnIndex,
			@ShellOption(value = "mergedFileColumnIndex", defaultValue = "-1") String mergedFileColumnIndex,
			@ShellOption(value = "newFileFullPath", defaultValue = "defaultValue") String newFileFullPath)
			throws IOException {

		log.info("Inside ReportJobCommands.removeDuplicatesFiles()");

		if ("-1".equalsIgnoreCase(sourceFileColumnIndex) || "-1".equalsIgnoreCase(mergedFileColumnIndex)) {

			throw new InvalidInputException("Column Index to compare cannot be negative");
		}

		List<Integer> sourceColumnIndexes = Stream.of(sourceFileColumnIndex.split(",")).map(String::trim)
				.mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		List<Integer> mergedColumnIndexes = Stream.of(mergedFileColumnIndex.split(",")).map(String::trim)
				.mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		if (sourceColumnIndexes.size() != mergedColumnIndexes.size()) {

			throw new InvalidInputException("ColumnIndexes should match in both source and merged files");
		}

		String sourceFile = sourceFileFullPath;
		BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));

		String line = null;

		sourceReader.readLine();
		List<String[]> sourceFileRowList = new ArrayList<String[]>();
		while ((line = sourceReader.readLine()) != null) {

			sourceFileRowList.add(convertRowToArray(line));
		}

		log.info("Sorting sourceFileRowList started with indexes -> {}", sourceColumnIndexes);
		Collections.sort(sourceFileRowList, compareByMultiIndexes(sourceColumnIndexes));
		log.info("Sorting sourceFileRowList completed with indexes -> {}", sourceColumnIndexes);

		sourceReader.close();

		String orignalMergedFile = mergedFileFullPath;
		BufferedReader originalMergedReader = new BufferedReader(new FileReader(orignalMergedFile));

		line = null;

		String headerLine = originalMergedReader.readLine();
		List<String[]> mergedFileRowList = new ArrayList<String[]>();
		while ((line = originalMergedReader.readLine()) != null) {

			mergedFileRowList.add(convertRowToArray(line));
		}

		log.info("Sorting mergedFileRowList started with indexes -> {}", mergedColumnIndexes);
		Collections.sort(mergedFileRowList, compareByMultiIndexes(mergedColumnIndexes));
		log.info("Sorting mergedFileRowList completed with indexes -> {}", mergedColumnIndexes);

		originalMergedReader.close();

		List<String[]> cleanFileRowList = new ArrayList<String[]>();

		for (String[] mergedRowData : mergedFileRowList) {

			StringBuilder mergeRowBuilder = new StringBuilder();

			for (int index = 0; index < mergedColumnIndexes.size(); index++) {

				mergeRowBuilder.append(mergedRowData[mergedColumnIndexes.get(index)].trim().toLowerCase());
			}

			boolean foundSourceRowInMergedData = false;
			for (String[] sourceRowData : sourceFileRowList) {

				StringBuilder sourceRowBuilder = new StringBuilder();
				for (int index = 0; index < sourceColumnIndexes.size(); index++) {

					sourceRowBuilder.append(sourceRowData[sourceColumnIndexes.get(index)].trim().toLowerCase());

				}

				if (mergeRowBuilder.toString().equalsIgnoreCase(sourceRowBuilder.toString())) {

					foundSourceRowInMergedData = true;
					break;
				}
			}

			if (!foundSourceRowInMergedData) {

				StringBuilder logBuilder = new StringBuilder();

				for (int index = 0; index < mergedColumnIndexes.size(); index++) {

					logBuilder.append(mergedRowData[index]);
					logBuilder.append(",");
				}
				log.info("Adding mergedRowData -> {} to the final cleanup/todo list",
						logBuilder.substring(0, logBuilder.length() - 1));
				cleanFileRowList.add(mergedRowData);
			}
		}

		log.info("Total cleanup count with no header is " + cleanFileRowList.size());
		log.info("Total Original/Merged with no header count is " + mergedFileRowList.size());
		log.info("Total Extract/Sqlite with no header count is " + sourceFileRowList.size());

		if ((mergedFileRowList.size() - sourceFileRowList.size()) != cleanFileRowList.size()) {

			log.error(" ---------- Count does not match ---------- ");
		}

		FileWriter fstream1 = new FileWriter(newFileFullPath); // creating
																// a
																// new
																// file
																// writer.
		BufferedWriter out = new BufferedWriter(fstream1);
		out.write(headerLine);
		out.newLine();
		for (String[] lineAsArray : cleanFileRowList) {

			String lineAsStr = String.join(",", lineAsArray);
			out.write(lineAsStr);
			out.newLine();
		}
		out.close();

	}

	private static Comparator<String[]> compareByMultiIndexes(List<Integer> columnIndexes) {

		Comparator<String[]> comparator = compareByIndex(columnIndexes.get(0));

		if (columnIndexes.size() > 1) {

			for (int index = 1; index < columnIndexes.size(); index++) {

				comparator = comparator.thenComparing(compareByIndex(index));

			}
		}

		return comparator;
	}

	private static Comparator<String[]> compareByIndex(int columnIndex) {

		Comparator<String[]> comparator = new Comparator<String[]>() {
			@Override
			public int compare(String[] p1, String[] p2) {
				return p1[columnIndex].trim().toLowerCase().compareTo(p2[columnIndex].trim().toLowerCase());
			}
		};

		return comparator;
	}

	private static String[] convertRowToArray(String item) {

		List<String> itemColumnList = Stream.of(item.split(",")).map(String::trim).collect(Collectors.toList());

		String[] itemColumnArray = new String[itemColumnList.size()];

		return itemColumnList.toArray(itemColumnArray);

	}

	@ShellMethod(value = "RemoveDuplicates Job, validCommand is -> removeduplicatesmap sourceFileFullPath $sourceFileFullPath sourceFileColumnIndex $sourceFileColumnIndex mergedFileFullPath $mergedFileFullPath mergedFileColumnIndex $mergedFileColumnIndex newFileFullPath $newFileFullPath", key = "removeduplicatesmap")
	public void removeDuplicatesFilesUsingMap(
			@ShellOption(value = "sourceFileFullPath", defaultValue = "defaultValue") String sourceFileFullPath,
			@ShellOption(value = "mergedFileFullPath", defaultValue = "defaultValue") String mergedFileFullPath,
			@ShellOption(value = "sourceFileColumnIndex", defaultValue = "-1") String sourceFileColumnIndex,
			@ShellOption(value = "mergedFileColumnIndex", defaultValue = "-1") String mergedFileColumnIndex,
			@ShellOption(value = "newFileFullPath", defaultValue = "defaultValue") String newFileFullPath)
			throws IOException {

		log.info("Inside ReportJobCommands.removeDuplicatesFilesUsingMap()");
		if ("-1".equalsIgnoreCase(sourceFileColumnIndex) || "-1".equalsIgnoreCase(mergedFileColumnIndex)) {

			throw new InvalidInputException("Column Index to compare cannot be negative");
		}

		List<Integer> sourceColumnIndexes = Stream.of(sourceFileColumnIndex.split(",")).map(String::trim)
				.mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		List<Integer> mergedColumnIndexes = Stream.of(mergedFileColumnIndex.split(",")).map(String::trim)
				.mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		if (sourceColumnIndexes.size() != mergedColumnIndexes.size()) {

			throw new InvalidInputException("ColumnIndexes should match in both source and merged files");
		}

		String sourceFile = sourceFileFullPath;
		BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));

		String line = null;

		sourceReader.readLine();
		List<String[]> sourceFileRowList = new ArrayList<String[]>();
		while ((line = sourceReader.readLine()) != null) {

			sourceFileRowList.add(convertRowToArray(line));
		}

		log.info("Sorting sourceFileRowList started with indexes -> {}", sourceColumnIndexes);
		Collections.sort(sourceFileRowList, compareByMultiIndexes(sourceColumnIndexes));
		log.info("Sorting sourceFileRowList completed with indexes -> {}", sourceColumnIndexes);

		sourceReader.close();

		String orignalMergedFile = mergedFileFullPath;
		BufferedReader originalMergedReader = new BufferedReader(new FileReader(orignalMergedFile));

		line = null;

		String headerLine = originalMergedReader.readLine();
		List<String[]> mergedFileRowList = new ArrayList<String[]>();
		while ((line = originalMergedReader.readLine()) != null) {

			mergedFileRowList.add(convertRowToArray(line));
		}

		log.info("Sorting mergedFileRowList started with indexes -> {}", mergedColumnIndexes);
		Collections.sort(mergedFileRowList, compareByMultiIndexes(mergedColumnIndexes));
		log.info("Sorting mergedFileRowList completed with indexes -> {}", mergedColumnIndexes);

		originalMergedReader.close();

		List<String[]> cleanFileRowList = new ArrayList<String[]>();

		Map<String, String[]> sourceKeyMap = new HashMap<String, String[]>();
		for (String[] sourceFileRowData : sourceFileRowList) {

			StringBuilder sourceFileMapKeyBuilder = new StringBuilder();

			for (int index = 0; index < sourceColumnIndexes.size(); index++) {

				sourceFileMapKeyBuilder.append(sourceFileRowData[sourceColumnIndexes.get(index)].trim().toLowerCase());
			}

			if (null != sourceKeyMap.get(sourceFileMapKeyBuilder.toString().toLowerCase())) {

				log.info("{} already exist in the sourceMap ", sourceFileMapKeyBuilder.toString().toLowerCase());
			}
			sourceKeyMap.put(sourceFileMapKeyBuilder.toString().toLowerCase(), sourceFileRowData);
		}

		sourceKeyMap.forEach((key, value) -> {

			log.info("key of sourceKeyMap/extract is {}", key);
		});

		log.info("sourceKeyMap/extract keyset size before checking for duplicates is {}", sourceKeyMap.keySet().size());
		List<String> sourceKeysWithoutDuplicates = sourceKeyMap.keySet().stream().distinct()
				.collect(Collectors.toList());
		log.info("sourceKeyMap/extract keyset size after removing duplicates is {}",
				sourceKeysWithoutDuplicates.size());

		Map<String, String[]> mergeKeyMap = new HashMap<String, String[]>();

		int newRecordCounter = 1;
		for (String[] mergedRowData : mergedFileRowList) {

			StringBuilder mergeFileMapKeyBuilder = new StringBuilder();

			for (int index = 0; index < mergedColumnIndexes.size(); index++) {

				mergeFileMapKeyBuilder.append(mergedRowData[mergedColumnIndexes.get(index)].trim().toLowerCase());
			}

			if (null != mergeKeyMap.get(mergeFileMapKeyBuilder.toString().toLowerCase())) {

				log.info("{} already exist in the sourceMap ", mergeFileMapKeyBuilder.toString().toLowerCase());
			}
			mergeKeyMap.put(mergeFileMapKeyBuilder.toString().toLowerCase(), mergedRowData);

			if (null == sourceKeyMap.get(mergeFileMapKeyBuilder.toString().toLowerCase())) {

				StringBuilder logBuilder = new StringBuilder();

				for (int index = 0; index < mergedColumnIndexes.size(); index++) {

					logBuilder.append(mergedRowData[mergedColumnIndexes.get(index)]);
					logBuilder.append(",");
				}
				log.info("Number of records  {}", newRecordCounter);
				log.info("Adding mergedRowData -> {} to the final cleanup/todo list",
						logBuilder.substring(0, logBuilder.length() - 1));

				cleanFileRowList.add(mergedRowData);

				newRecordCounter = newRecordCounter + 1;
			}
		}

		mergeKeyMap.forEach((key, value) -> {

			log.info("key of mergeKeyMap is {}", key);
		});

		log.info("mergeKeyMap keyset size before checking for duplicates is {}", mergeKeyMap.keySet().size());
		List<String> mergeKeysWithoutDuplicates = mergeKeyMap.keySet().stream().distinct().collect(Collectors.toList());
		log.info("mergeKeyMap keyset size after removing duplicates is {}", mergeKeysWithoutDuplicates.size());

		log.info("Total cleanup count with no header is " + cleanFileRowList.size());
		log.info("Total Original/Merged with no header count is " + mergedFileRowList.size());
		log.info("Total Extract/Sqlite with no header count is " + sourceFileRowList.size());

		if ((mergedFileRowList.size() - sourceFileRowList.size()) != cleanFileRowList.size()) {

			log.error(" ---------- Count does not match ---------- ");
		}

		FileWriter fstream1 = new FileWriter(newFileFullPath); // creating
																// a
																// new
																// file
																// writer.
		BufferedWriter out = new BufferedWriter(fstream1);
		out.write(headerLine);
		out.newLine();
		for (String[] lineAsArray : cleanFileRowList) {

			String lineAsStr = String.join(",", lineAsArray);
			out.write(lineAsStr);
			out.newLine();
		}
		out.close();

	}

	private void validateBatchParametersAndTriggerService(String batchStartDateTime, String batchEndDateTime,
			Integer numberOfHours, BatchTriggerInformation batchTriggerInformation, String jobType, String jobCommand)
			throws Exception {

		if (!"defaultValue".equalsIgnoreCase(batchStartDateTime)) {

			if (batchStartDateTime.contains("T")) {

				DateTimeUtil.isValidDateTime(batchStartDateTime);
			} else {

				DateTimeUtil.isValidDate(batchStartDateTime);
				batchStartDateTime = batchStartDateTime + "T00:00:00.0000000Z";
				DateTimeUtil.isValidDateTime(batchStartDateTime);
			}

			if (!"defaultValue".equalsIgnoreCase(batchEndDateTime)) {

				if (batchEndDateTime.contains("T")) {

					DateTimeUtil.isValidDateTime(batchEndDateTime);
				} else {

					DateTimeUtil.isValidDate(batchEndDateTime);
					batchEndDateTime = batchEndDateTime + "T23:59:59.9999999Z";
					DateTimeUtil.isValidDateTime(batchEndDateTime);
				}

				batchTriggerInformation.setJobType(jobType);
				batchTriggerInformation.setBatchStartDateTime(batchStartDateTime);
				batchTriggerInformation.setBatchEndDateTime(batchEndDateTime);
			} else {

				if (numberOfHours == -1) {
					throw new InvalidInputException(
							"Input is wrong to trigger this batch, numberOfHours cannot be -1 with current command");
				}

				batchTriggerInformation.setJobType(jobType);
				batchTriggerInformation.setBatchStartDateTime(batchStartDateTime);
				batchTriggerInformation.setNumberOfHours(numberOfHours);
			}
		} else if (numberOfHours > -1) {

			batchTriggerInformation.setJobType(jobType);
			batchTriggerInformation.setNumberOfHours(numberOfHours);
		} else {

			log.info("ValidCommand sample is -> {}", jobCommand);

			log.error(" !!!!!!!!!!!!!!!!!!!! Inputs are wrong to trigger {} batch !!!!!!!!!!!!!!!!!!!! ", jobType);
			throw new InvalidInputException(
					" !!!!!!!!!!!!!!!!!!!! Inputs are wrong to trigger" + jobType + " batch !!!!!!!!!!!!!!!!!!!! ");
		}

		log.info("Validation done in ReportJobCommands, now calling batchTriggerService for BatchType -> {}", jobType);
		batchTriggerService.callService(batchTriggerInformation);
	}

}