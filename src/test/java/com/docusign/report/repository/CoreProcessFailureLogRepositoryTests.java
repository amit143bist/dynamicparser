package com.docusign.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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

import com.docusign.report.db.model.CoreProcessFailureLog;
import com.docusign.report.db.repository.CoreProcessFailureLogRepository;

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
public class CoreProcessFailureLogRepositoryTests {

	@Autowired
	private CoreProcessFailureLogRepository coreProcessFailureLogRepository;

	@Test
	public void testfindAllByFailureRecordIdAndRetryStatusIsNullOrRetryStatus() {

		Iterable<CoreProcessFailureLog> processFailureLogIter = coreProcessFailureLogRepository
				.findAllByFailureRecordIdAndRetryStatusOrFailureRecordIdAndRetryStatusIsNull("1234", "F", "1234");

		assertThat(IterableUtil.isNullOrEmpty(processFailureLogIter)).isFalse();
		assertThat(IterableUtil.sizeOf(processFailureLogIter)).isEqualTo(2);

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testfindAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull() {

		Iterable<CoreProcessFailureLog> processFailureLogIter = coreProcessFailureLogRepository
				.findAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull("b4ad9898-dd2f-43d4-b685-dd08aebc5065",
						"F", "b4ad9898-dd2f-43d4-b685-dd08aebc5065");

		assertThat(IterableUtil.isNullOrEmpty(processFailureLogIter)).isFalse();
		assertThat(IterableUtil.sizeOf(processFailureLogIter)).isEqualTo(2);

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void findAllByBatchIdIn() {

		List<String> batchIds = new ArrayList<String>();
		batchIds.add("b4ad9898-dd2f-43d4-b685-dd08aebc5065");

		Iterable<CoreProcessFailureLog> processFailureLogIter = coreProcessFailureLogRepository
				.findAllByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(batchIds, "F", batchIds);

		assertThat(IterableUtil.isNullOrEmpty(processFailureLogIter)).isFalse();
		assertThat(IterableUtil.sizeOf(processFailureLogIter)).isEqualTo(2);

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void countByByBatchIdIn() {

		List<String> batchIds = new ArrayList<String>();
		batchIds.add("b4ad9898-dd2f-43d4-b685-dd08aebc5065");

		Long processFailureLogCount = coreProcessFailureLogRepository
				.countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(batchIds, "F", batchIds);

		assertThat(processFailureLogCount).isNotNull();
		assertThat(processFailureLogCount).isEqualTo(2);

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void findAllByRetryStatusOrRetryStatusIsNull() {

		Iterable<CoreProcessFailureLog> processFailureLogIter = coreProcessFailureLogRepository
				.findAllByRetryStatusOrRetryStatusIsNull("F");

		assertThat(IterableUtil.isNullOrEmpty(processFailureLogIter)).isFalse();
		assertThat(IterableUtil.sizeOf(processFailureLogIter)).isEqualTo(2);

	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void countByRetryStatusOrRetryStatusIsNull() {

		Long processFailureLogCount = coreProcessFailureLogRepository.countByRetryStatusOrRetryStatusIsNull("F");

		assertThat(processFailureLogCount).isNotNull();
		assertThat(processFailureLogCount).isEqualTo(2);

	}

}