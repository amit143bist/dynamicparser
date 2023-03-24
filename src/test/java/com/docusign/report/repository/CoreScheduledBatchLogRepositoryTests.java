package com.docusign.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.assertj.core.util.IterableUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.docusign.report.db.model.CoreScheduledBatchLog;
import com.docusign.report.db.repository.CoreScheduledBatchLogRepository;
import com.docusign.report.utils.DateTimeUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED + "=false",
		InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",
		"spring.cloud.config.enabled=false" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@TestPropertySource(locations = "classpath:application-unittest.yml")
@SqlGroup({
		@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sqlscripts/beforeTestRun.sql"),
		@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
public class CoreScheduledBatchLogRepositoryTests {

	@Autowired
	private CoreScheduledBatchLogRepository coreScheduledBatchLogRepository;

	@Test
	public void test_findAllByBatchTypeAndBatchEndDateTimeIsNull() {

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchTypeAndBatchEndDateTimeIsNull("envelopepull");

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(1);
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void test_findAllByBatchTypeAndBatchEndDateTimeIsNull_1() {

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchTypeAndBatchEndDateTimeIsNull("envelopepull");

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(5);
	}

	@Test
	public void test_findTopByBatchTypeOrderByBatchStartDateTimeDesc() {

		Optional<CoreScheduledBatchLog> scheduledBatchLogOptional = coreScheduledBatchLogRepository
				.findTopByBatchTypeOrderByBatchStartDateTimeDesc("envelopepull");

		assertThat(scheduledBatchLogOptional).isNotNull();

		scheduledBatchLogOptional.map(scheduledBatchLog -> {

			assertThat(scheduledBatchLog.getBatchId()).isEqualTo("e781ca58-dec7-44b7-a312-5c21fded402f");
			return null;
		});

	}

	@Test
	public void test_findAllByBatchTypeAndBatchEndDateTimeIsNotNull() {

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchTypeAndBatchEndDateTimeIsNotNull("envelopepull");

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(2);
	}

	@Test
	public void test_findAllByBatchType() {

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchType("envelopepull");

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(3);
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void test_findAllByBatchTypeAndBatchStartDateTimeBetween() {

		Long fromDate = DateTimeUtil.convertToEpochTimeFromDateTimeAsString("2020-01-10T00:00:00.8100000Z");
		Long toDate = DateTimeUtil.convertToEpochTimeFromDateTimeAsString("2020-03-01T23:59:59.8100000Z");

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository
				.findAllByBatchTypeAndBatchStartDateTimeBetween("envelopepull", fromDate, toDate);

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(7);
	}

	@Test
	public void test_findAll() {

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = coreScheduledBatchLogRepository.findAll();

		assertThat(scheduledBatchLogList).isNotNull();

		assertThat(IterableUtil.sizeOf(scheduledBatchLogList)).isEqualTo(4);
	}

}