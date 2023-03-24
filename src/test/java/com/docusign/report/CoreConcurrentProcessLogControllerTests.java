package com.docusign.report;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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

import com.docusign.report.common.constant.ProcessStatus;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
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
public class CoreConcurrentProcessLogControllerTests extends AbstractTests {

	@Test
	public void testCoreConcurrentProcessLogController_validUser_saveConcurrentProcess1() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/report/scheduledbatch/concurrentprocess")
				.content(asJsonString(new ConcurrentProcessLogDefinition(null, "e781ca58-dec7-44b7-a312-5c21fded402f",
						DateTimeUtil.currentEpochTime(), null, ProcessStatus.INPROGRESS.toString(), Long.valueOf(40),
						UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString())))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.processId").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_validUser_updateConcurrentProcess() throws Exception {

		ConcurrentProcessLogDefinition concurrentProcessLogRequest = new ConcurrentProcessLogDefinition();
		concurrentProcessLogRequest.setProcessStatus(ProcessStatus.COMPLETED.toString());
		mockMvc.perform(MockMvcRequestBuilders
				.put("/report/scheduledbatch/concurrentprocess/processes/84a3a1d3-02e0-4ca5-a5bc-590f37e0835e")
				.content(asJsonString(concurrentProcessLogRequest)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.processEndDateTime").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_validUser_countPendingConcurrentProcessInBatch()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/countpendingprocesses/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$").value(2));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_inValidBatchId_countPendingConcurrentProcessInBatch()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/countpendingprocesses/84a3a1d3-02e0-4ca5-a5bc-590f37e0835e")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$").value(0));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_findAllProcessesForBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/processes/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_findAllInCompleteProcessesForBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get(
				"/report/scheduledbatch/concurrentprocess/incompleteprocesses/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_findAllCompleteProcessesForBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/completeprocesses/e781ca58-dec7-44b7-a312-5c21fded402d")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_findAllCompleteProcessesForBatchId_2() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/completeprocesses/b4ad9898-dd2f-43d4-b685-dd08aebc5067")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessLogDefinitions", hasSize(1)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreConcurrentProcessLogController_findProcessByProcessId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/concurrentprocess/process/84a3a1d3-02e0-4ca5-a5bc-590f37e0836e")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("b4ad9898-dd2f-43d4-b685-dd08aebc5065"));
	}
}