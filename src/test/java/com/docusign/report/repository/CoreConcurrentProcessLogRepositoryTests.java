package com.docusign.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.docusign.report.db.model.CoreConcurrentProcessLog;
import com.docusign.report.db.repository.CoreConcurrentProcessLogRepository;

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
public class CoreConcurrentProcessLogRepositoryTests {

	@Autowired
	private CoreConcurrentProcessLogRepository coreConcurrentProcessLogRepository;

	@Test
	public void testCountByBatchIdAndProcessEndDateTime() {

		Long count = coreConcurrentProcessLogRepository
				.countByBatchIdAndProcessEndDateTimeIsNull("e781ca58-dec7-44b7-a312-5c21fded402d");
		assertThat(count).isNotNull();
		assertThat(count).isEqualTo(1);
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testfindAllByBatchId() {

		Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogList = coreConcurrentProcessLogRepository
				.findAllByBatchId("b4ad9898-dd2f-43d4-b685-dd08aebc5065");
		assertThat(coreConcurrentProcessLogList).isNotNull();
		assertThat(IterableUtil.sizeOf(coreConcurrentProcessLogList)).isEqualTo(2);

	}

	@Test
	public void testfindAllByBatchIdAndProcessEndDateTimeIsNull() {

		Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogList = coreConcurrentProcessLogRepository
				.findAllByBatchIdAndProcessEndDateTimeIsNull("e781ca58-dec7-44b7-a312-5c21fded402d");
		assertThat(coreConcurrentProcessLogList).isNotNull();
		assertThat(IterableUtil.sizeOf(coreConcurrentProcessLogList)).isEqualTo(1);
	}
}