package com.docusign.report;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.docusign.report.db.domain.ScheduledBatchLogRequest;
import com.docusign.report.domain.BatchStartParams;
import com.docusign.report.utils.DateTimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class CoreScheduledBatchLogControllerTests extends AbstractTests {

	@Test
	public void testCoreScheduledBatchLogController_validRole_findInCompleteBatch3() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402d"));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findInCompleteBatch4() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402d"));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findAllInCompleteBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/incompletebatches/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(5)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllCompleteBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/completebatches/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(2)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllBatchesByBatchType() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/batches/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(3)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/batches")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(4)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findLatestBatchByBatchType() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/report/scheduledbatch/latestbatch/batchtype/envelopepull")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402f"));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_saveBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/report/scheduledbatch")
				.content(asJsonString(new ScheduledBatchLogRequest("envelopepull",
						new ObjectMapper().writeValueAsString(new BatchStartParams(
								DateTimeUtil.convertToEpochTimeFromDateTimeAsString("2020-02-27T20:06:00.8100000Z"),
								DateTimeUtil.currentEpochTime(), 50)),
						Long.valueOf(20))))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.batchId").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_updateBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.put("/report/scheduledbatch/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty()).andExpect(jsonPath("$.batchEndDateTime").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findBatchByBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/latestbatch/batchid/b4ad9898-dd2f-43d4-b685-dd08aebc5063")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").exists()).andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("b4ad9898-dd2f-43d4-b685-dd08aebc5063"));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findAllByBatchTypeAndBatchStartDateTimeBetween()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/report/scheduledbatch/batches/batchtype/envelopepull/fromdate/2020-01-10/todate/2020-03-01")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(7)));
	}

}