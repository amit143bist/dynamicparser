package com.docusign.report;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.docusign.report.common.constant.RetryStatus;
import com.docusign.report.db.domain.ConcurrentProcessFailureLogDefinition;
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
@AutoConfigureMockMvc
public class CoreProcessFailureLogControllerTests extends AbstractTests {

	@Test
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/concurrentprocessfailure")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLogForBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocessfailure/batch/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLogForFailureRecordId()
			throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.get("/report/scheduledbatch/concurrentprocessfailure/failurerecords/1234")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_saveBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/report/scheduledbatch/concurrentprocessfailure")
				.content(asJsonString(new ConcurrentProcessFailureLogDefinition(null,
						"84a3a1d3-02e0-4ca5-a5bc-590f37e0834e", "ERROR_13", "Test Error",
						DateTimeUtil.currentTimeInString(), null, "9999", "FETCH_PRONTO_DOC", null, null)))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.processFailureId").isNotEmpty());
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_1() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.F.toString());
		concurrentProcessFailureLogRequest.setRetryCount(Long.valueOf(1));
		mockMvc.perform(MockMvcRequestBuilders
				.put("/report/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isNotEmpty()).andExpect(jsonPath("$.message")
						.value("FailureDateTime is null for processFailureId# f228254a-6f82-4ec1-9c8a-44a1a20577f1"));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_2() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.F.toString());
		concurrentProcessFailureLogRequest.setRetryCount(Long.valueOf(1));
		concurrentProcessFailureLogRequest.setFailureDateTime(DateTimeUtil.currentTimeInString());
		mockMvc.perform(MockMvcRequestBuilders
				.put("/report/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.failureDateTime").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/concurrentprocessfailure")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_listAllBatchFailuresByBatchIds() throws Exception {

		List<String> batchIds = new ArrayList<String>();
		batchIds.add("b4ad9898-dd2f-43d4-b685-dd08aebc5065");

		mockMvc.perform(MockMvcRequestBuilders.put("/report/scheduledbatch/concurrentprocessfailure/failurerecords")
				.content(asJsonString(batchIds)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_countProcessFailuresByBatchIds() throws Exception {

		List<String> batchIds = new ArrayList<String>();
		batchIds.add("b4ad9898-dd2f-43d4-b685-dd08aebc5065");

		mockMvc.perform(MockMvcRequestBuilders
				.put("/report/scheduledbatch/concurrentprocessfailure/failurerecords/batchids/count")
				.content(asJsonString(batchIds)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$").value(2));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_countProcessFailures() throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.get("/report/scheduledbatch/concurrentprocessfailure/failurerecords/count")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$").value(4));
	}
}