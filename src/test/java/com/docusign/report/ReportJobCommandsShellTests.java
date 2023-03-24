package com.docusign.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

import org.assertj.core.util.IterableUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.RetryStatus;
import com.docusign.report.db.model.CoreConcurrentProcessLog;
import com.docusign.report.db.model.CoreScheduledBatchLog;
import com.docusign.report.db.repository.CoreConcurrentProcessLogRepository;
import com.docusign.report.db.repository.CoreProcessFailureLogRepository;
import com.docusign.report.db.repository.CoreScheduledBatchLogRepository;
import com.docusign.report.domain.BatchStartParams;
import com.docusign.report.domain.BatchTriggerInformation;
import com.docusign.report.domain.PathParam;
import com.docusign.report.utils.DateTimeUtil;
import com.docusign.report.utils.ReportAppUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED + "=false",
		InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",
		"spring.cloud.config.enabled=false" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(value = "unittest")
@Import(TestApplicationRunner.class)
@Slf4j
public class ReportJobCommandsShellTests {

	@Autowired
	private Shell shell;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CoreScheduledBatchLogRepository coreScheduledBatchLogRepository;

	@Autowired
	private CoreConcurrentProcessLogRepository coreConcurrentProcessLogRepository;

	@Autowired
	private CoreProcessFailureLogRepository coreProcessFailureLogRepository;

	@Value("${app.test.csvPath}")
	private String csvPath;

	@Test
	public void test_JobCommands_GenerateReportTables() throws IOException, TimeoutException, InterruptedException {

		log.info(" ********************* Testing generateTables command ********************* ");

		assertThat(shell.evaluate(() -> "generateTables"));

		Thread.sleep(5000);

		SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from reportdata where 0=1");

		int columnCount = sqlRowSet.getMetaData().getColumnCount();

		assertThat(columnCount).isEqualTo(14);

		jdbcTemplate.batchUpdate("drop table reportdata", "delete from corecachedatalog");

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preShellTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void test_JobCommands_LoadEnvelopeData_WithBeginDateEndDate()
			throws IOException, TimeoutException, InterruptedException {

		log.info(" ********************* Testing triggerjob command ********************* ");

		try {

			SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from reportdata where 0=1");

			int columnCount = sqlRowSet.getMetaData().getColumnCount();

			assertThat(columnCount).isEqualTo(14);
		} catch (Exception exp) {

			assertThat(shell.evaluate(() -> "generateTables"));

			Thread.sleep(5000);
		}

		Long batchStartTime = DateTimeUtil.currentEpochTime();
		String startDate = "2020-01-01";
		String endDate = "2020-03-10";
		assertThat(shell.evaluate(() -> "triggerjob batchStartDateTime " + startDate + " batchEndDateTime " + endDate
				+ " jobType PREPAREDATA"));

		Thread.sleep(5000);

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchTypeAndBatchStartDateTimeBetween("ESIGNENVELOPELOAD", batchStartTime,
						DateTimeUtil.currentEpochTime());

		assertThat(scheduledBatchLogList).isNotNull();
		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(1);

		scheduledBatchLogList.forEach(scheduledBatchLog -> {

			assertThat(scheduledBatchLog.getBatchId()).isNotNull();
			assertThat(scheduledBatchLog.getBatchType()).isEqualTo("ESIGNENVELOPELOAD");

			log.info("CreatedBy {} and createdDateTime {}", scheduledBatchLog.getCreatedBy(),
					scheduledBatchLog.getCreatedDateTime());
			assertThat(scheduledBatchLog.getCreatedBy()).isNotNull();
			assertThat(scheduledBatchLog.getCreatedDateTime()).isNotNull();
			assertThat(scheduledBatchLog.getBatchEndDateTime()).isNotNull();
//			assertThat(scheduledBatchLog.getTotalRecords().intValue()).isEqualTo(4);

			try {
				BatchStartParams batchStartParams = objectMapper.readValue(scheduledBatchLog.getBatchStartParameters(),
						BatchStartParams.class);

				assertThat(batchStartParams).isNotNull();
				assertThat(batchStartParams.getBeginDateTime()).isEqualTo(
						DateTimeUtil.convertToEpochTimeFromDateTimeAsString(startDate + "T00:00:00.0000000Z"));
				assertThat(batchStartParams.getEndDateTime())
						.isEqualTo(DateTimeUtil.convertToEpochTimeFromDateTimeAsString(endDate + "T23:59:59.9999999Z"));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			Long pendingProcessCount = coreConcurrentProcessLogRepository
					.countByBatchIdAndProcessEndDateTimeIsNull(scheduledBatchLog.getBatchId());
			assertThat(pendingProcessCount).isEqualTo(0);

			Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogList = coreConcurrentProcessLogRepository
					.findAllByBatchId(scheduledBatchLog.getBatchId());
			assertThat(coreConcurrentProcessLogList).isNotNull();
//			assertThat(IterableUtil.sizeOf(coreConcurrentProcessLogList)).isEqualTo(12);
		});

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preShellTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void test_JobCommands_LoadEnvelopeData_WithNumberOfHours()
			throws IOException, TimeoutException, InterruptedException {

		log.info(" ********************* Testing triggerjob command ********************* ");

		try {

			SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from reportdata where 0=1");

			int columnCount = sqlRowSet.getMetaData().getColumnCount();

			assertThat(columnCount).isEqualTo(14);
		} catch (Exception exp) {

			assertThat(shell.evaluate(() -> "generateTables"));

			Thread.sleep(5000);
		}

		String startDate = "2020-01-01";
		String endDate = "2020-03-10";
		assertThat(shell.evaluate(() -> "triggerjob batchStartDateTime " + startDate + " batchEndDateTime " + endDate
				+ " jobType PREPAREDATA"));

		Thread.sleep(5000);

		Integer numberOfHours = 72;
		assertThat(shell.evaluate(() -> "triggerjob numberOfHours " + numberOfHours + " jobType PREPAREDATA"));

		Thread.sleep(5000);

		Optional<CoreScheduledBatchLog> scheduledBatchLogOptional = coreScheduledBatchLogRepository
				.findTopByBatchTypeOrderByBatchStartDateTimeDesc("ESIGNENVELOPELOAD");

		scheduledBatchLogOptional.map(scheduledBatchLog -> {

			assertThat(scheduledBatchLog.getBatchId()).isNotNull();
			assertThat(scheduledBatchLog.getBatchEndDateTime()).isNotNull();
			assertThat(scheduledBatchLog.getBatchType()).isEqualTo("ESIGNENVELOPELOAD");
//			assertThat(scheduledBatchLog.getTotalRecords().intValue()).isEqualTo(3);

			List<String> batchIds = new ArrayList<String>(1);
			batchIds.add(scheduledBatchLog.getBatchId());

			Long failureCount = coreProcessFailureLogRepository
					.countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(batchIds, RetryStatus.F.toString(),
							batchIds);
			assertThat(failureCount).isEqualTo(0);
			try {

				BatchStartParams batchStartParams = objectMapper.readValue(scheduledBatchLog.getBatchStartParameters(),
						BatchStartParams.class);

				assertThat(batchStartParams).isNotNull();
				assertThat(batchStartParams.getBeginDateTime())
						.isEqualTo(DateTimeUtil.convertToEpochTimeFromDateTimeAsString("2020-03-11T00:00:00.0000000Z"));
				assertThat(batchStartParams.getEndDateTime())
						.isEqualTo(DateTimeUtil.convertToEpochTimeFromDateTimeAsString("2020-03-13T23:59:59.9999999Z"));

				long startDateTimeEpochInSeconds = DateTimeUtil
						.convertToEpochTimeFromDateTimeAsString("2020-03-11T00:00:00.0000000Z", null);
				long endDateTimeEpochInSeconds = DateTimeUtil
						.convertToEpochTimeFromDateTimeAsString("2020-03-13T23:59:59.9999999Z", null);

				Object[] paramValuesArr = new Object[2];
				paramValuesArr[0] = startDateTimeEpochInSeconds;
				paramValuesArr[1] = endDateTimeEpochInSeconds;

				String selectSql = "select employeeid, senderemail, sendername, envelopeid, sentdate, recipientcount, documentcount, pagescount from reportdata where sentdate between ? and ?";
				List<Map<String, Object>> selectDataMapList = jdbcTemplate.queryForList(selectSql, paramValuesArr);

				selectDataMapList.forEach((selectDataMap) -> {

					selectDataMap.forEach((key, value) -> {

						log.info("ColumnName -> {} and columnValue -> {}", key, value);
					});

				});

//				assertThat(selectDataMapList.size()).isEqualTo(3);

			} catch (JsonProcessingException e) {

				e.printStackTrace();
			}

			Long pendingProcessCount = coreConcurrentProcessLogRepository
					.countByBatchIdAndProcessEndDateTimeIsNull(scheduledBatchLog.getBatchId());
			assertThat(pendingProcessCount).isEqualTo(0);

			Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogList = coreConcurrentProcessLogRepository
					.findAllByBatchId(scheduledBatchLog.getBatchId());
			assertThat(coreConcurrentProcessLogList).isNotNull();
			assertThat(IterableUtil.sizeOf(coreConcurrentProcessLogList)).isEqualTo(10);
			return null;
		});

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preShellTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void test_JobCommands_GenerateCSVReport() throws IOException, TimeoutException, InterruptedException {

		try {

			SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from reportdata where 0=1");

			int columnCount = sqlRowSet.getMetaData().getColumnCount();

			assertThat(columnCount).isEqualTo(14);
		} catch (Exception exp) {

			assertThat(shell.evaluate(() -> "generateTables"));

			Thread.sleep(5000);
		}

		String startDate = "2020-04-18";
		String endDate = DateTimeUtil.currentTimeInString();
		assertThat(shell.evaluate(() -> "triggerjob batchStartDateTime " + startDate + " batchEndDateTime " + endDate
				+ " jobType PREPAREDATA"));

		Thread.sleep(10000);

		BatchTriggerInformation batchTriggerInformation = new BatchTriggerInformation();
		List<PathParam> pathParamList = new ArrayList<PathParam>();

		PathParam pathParam = new PathParam();
		pathParam.setParamName("csvDownloadFolderPath");
		pathParam.setParamValue(csvPath);
		pathParamList.add(pathParam);

		pathParam = new PathParam();
		pathParam.setParamName("disableCurrentDateInCSVFolderPath");
		pathParam.setParamValue("true");
		pathParamList.add(pathParam);

		batchTriggerInformation.setPathParams(pathParamList);

		String batchTriggerInfoAsJSON = objectMapper.writeValueAsString(batchTriggerInformation);

		assertThat(shell.evaluate(() -> "triggerjob batchStartDateTime " + startDate + " batchEndDateTime " + endDate
				+ " jobType MANAGEDATA" + " dynamicParams " + batchTriggerInfoAsJSON));

		Thread.sleep(5000);

		PathParam disableCsvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DISABLE_CURR_DATE_IN_CSV_FOLDER_PATH_FLAG);

		String csvFolderPath = null;
		if (null != disableCsvFilePathParam && "true".equalsIgnoreCase(disableCsvFilePathParam.getParamValue())) {

			csvFolderPath = csvPath;
		} else {

			csvFolderPath = csvPath + File.separator + DateTimeUtil.currentDateInString(TimeZone.getDefault().getID());
		}

		File[] csvFolderFiles = new File(csvFolderPath).listFiles();

		for (File csvFile : csvFolderFiles) {

			log.info("CSV File {} created", csvFile.getName());
		}
		assertThat(csvFolderFiles.length).isGreaterThan(0);
	}
}