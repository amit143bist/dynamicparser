package com.docusign.report.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.docusign.report.common.constant.ProcessStatus;
import com.docusign.report.common.constant.ValidationResult;
import com.docusign.report.common.exception.JSONConversionException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.db.controller.CoreConcurrentProcessLogController;
import com.docusign.report.db.controller.CoreProcessFailureLogController;
import com.docusign.report.db.controller.CoreScheduledBatchLogController;
import com.docusign.report.db.domain.ConcurrentProcessFailureLogDefinition;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.db.domain.ConcurrentProcessLogsInformation;
import com.docusign.report.db.domain.ScheduledBatchLogRequest;
import com.docusign.report.db.domain.ScheduledBatchLogResponse;
import com.docusign.report.domain.BatchResultInformation;
import com.docusign.report.domain.BatchStartParams;
import com.docusign.report.domain.ReportRunArgs;
import com.docusign.report.utils.DateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchDataService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	CoreProcessFailureLogController coreProcessFailureLogController;

	@Autowired
	CoreScheduledBatchLogController coreScheduledBatchLogController;

	@Autowired
	CoreConcurrentProcessLogController coreConcurrentProcessLogController;

	@Value("${app.authorization.userId}")
	private String authUserId;

	public String createBatchJob(String batchType, BatchStartParams batchStartParams, Long totalRecords) {

		log.info("Creating BatchJob for batchType -> {} and totalRecords -> {}", batchType, totalRecords);

		ScheduledBatchLogRequest scheduledBatchLogRequest = new ScheduledBatchLogRequest();

		scheduledBatchLogRequest.setBatchType(batchType);
		try {
			scheduledBatchLogRequest.setBatchStartParameters(objectMapper.writeValueAsString(batchStartParams));
		} catch (JsonProcessingException e) {

			log.error(
					"JSON Mapping error occured in converting to BatchStartParams string for object {} in createBatchJob",
					batchStartParams);
			throw new JSONConversionException(
					"JSON Mapping error occured in converting to BatchStartParams in createBatchJob", e.getCause());
		}
		scheduledBatchLogRequest.setTotalRecords(totalRecords);

		return coreScheduledBatchLogController.saveBatch(scheduledBatchLogRequest).getBody().getBatchId();
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId, String groupId,
			String accountId, String userId) {

		log.info("New ConcurrentProcess created with batchSize {} for batchId -> {} and groupId -> {}", batchSize,
				batchId, groupId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setGroupId(groupId);
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);
		concurrentProcessLogDefinition.setAccountId(accountId);
		concurrentProcessLogDefinition.setUserId(userId);

		return coreConcurrentProcessLogController.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();
	}

	public void finishConcurrentProcess(String processId) {

		log.info("Finishing ConcurrentProcess for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());

		coreConcurrentProcessLogController.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();

	}

	public void finishConcurrentProcessWithTotalRecords(String processId) {

		log.info("Finishing ConcurrentProcess with totalrecords for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());
		try {

			Long totalRecordsInGroup = coreConcurrentProcessLogController.countTotalRecordsInGroup(processId).getBody();

			concurrentProcessLogDefinition.setTotalRecordsInProcess(totalRecordsInGroup);
			coreConcurrentProcessLogController.updateConcurrentProcess(concurrentProcessLogDefinition, processId)
					.getBody();
		} catch (ResourceNotFoundException exp) {

			log.error(
					" ~~~~~~~~~~~~~~~~~~~~~~~~~ Cound not find any process with the groupId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
					processId);
		}

	}

	public void publishBatchData(String batchId, boolean failureReported,
			BatchResultInformation batchResultInformation) {

		ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch = coreConcurrentProcessLogController
				.findAllProcessesForBatchId(batchId).getBody();

		List<String> successAccountIds = new ArrayList<String>();
		List<String> failedAccountIds = new ArrayList<String>();
		if (null != concurrentProcessLogsInformationForBatch
				&& null != concurrentProcessLogsInformationForBatch.getConcurrentProcessLogDefinitions()) {

			log.info(
					" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<< {} is the total concurrent threads used to run the batchId -> {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>> ",
					concurrentProcessLogsInformationForBatch.getConcurrentProcessLogDefinitions().size(), batchId);

		}

		if (failureReported) {

			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails = coreConcurrentProcessLogController
					.findAllParentGroups(batchId).getBody();

			log.info(
					" ------------------------------ ------------------------------ ------------------------------ ------------------------------ ");
			log.error(
					" ------------------------------ Failure Reported for batchId -> {} ------------------------------ ",
					batchId);
			if (null != concurrentProcessLogsInformationAccountDetails
					&& null != concurrentProcessLogsInformationAccountDetails.getConcurrentProcessLogDefinitions()) {

				List<ConcurrentProcessLogDefinition> processAccountInfoList = concurrentProcessLogsInformationAccountDetails
						.getConcurrentProcessLogDefinitions();

				log.info(
						" #################### Total Accounts processed for batchId -> {} are {} #################### ",
						batchId, processAccountInfoList.size());

				processAccountInfoList.forEach(accountInfo -> {

					if (null != accountInfo.getProcessEndDateTime()) {

						log.info(
								" #################### AccountId -> {} has successfully processed {} records #################### ",
								accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());
						successAccountIds.add(accountInfo.getGroupId());
					} else {

						log.error(" #################### AccountId -> {} has reported error #################### ",
								accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());
						failedAccountIds.add(accountInfo.getGroupId());
					}

				});

			}

			showFailedBatchRecordsDetails(batchId, concurrentProcessLogsInformationForBatch);

			log.info(
					" ------------------------------ ------------------------------ ------------------------------ ------------------------------ ");
		} else {

			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails = coreConcurrentProcessLogController
					.findAllSuccessParentGroups(batchId).getBody();

			if (null != concurrentProcessLogsInformationAccountDetails
					&& null != concurrentProcessLogsInformationAccountDetails.getConcurrentProcessLogDefinitions()) {

				List<ConcurrentProcessLogDefinition> processAccountInfoList = concurrentProcessLogsInformationAccountDetails
						.getConcurrentProcessLogDefinitions();

				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ");
				log.info(
						" #################### Total Accounts successfully processed for batchId -> {} are {} #################### ",
						batchId, processAccountInfoList.size());

				processAccountInfoList.forEach(accountInfo -> {

					log.info(
							" #################### AccountId -> {} has successfully processed {} records #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());

					successAccountIds.add(accountInfo.getGroupId());
				});
				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ");

			}
		}

		batchResultInformation.setSuccessAccountIds(successAccountIds);
		batchResultInformation.setFailedAccountIds(failedAccountIds);

	}

	private void showFailedBatchRecordsDetails(String batchId,
			ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch) {

		if (null != concurrentProcessLogsInformationForBatch) {

			List<ConcurrentProcessLogDefinition> processList = concurrentProcessLogsInformationForBatch
					.getConcurrentProcessLogDefinitions();

			processList.forEach(processInfo -> {

				if (null != processInfo.getProcessEndDateTime()) {

					log.info(" :::::::::::::::::::::::::::::: Success thread details :::::::::::::::::::::::::::::: ");
					if (null != processInfo.getAccountId()) {

						log.info("AccountId -> {} is successfully completed using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);
					} else {

						log.info("AccountId -> {} is successfully completed using processId -> {} for batchId -> {}",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId);
					}

				} else {

					log.info(" :::::::::::::::::::::::::::::: Failed thread details :::::::::::::::::::::::::::::: ");
					if (null != processInfo.getAccountId()) {

						log.info("AccountId -> {} is unsuccessfully processed using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);
					} else {

						log.info("AccountId -> {} is unsuccessfully completed using processId -> {} for batchId -> {}",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId);
					}

				}
			});
		}
	}

	public void finishBatchProcess(String batchId, Long totalRecords) {

		coreScheduledBatchLogController.updateBatch(batchId, totalRecords);
	}

	public void finishBatchProcess(String batchId) {

		ResponseEntity<Long> processCount = coreConcurrentProcessLogController
				.countPendingConcurrentProcessInBatch(batchId);

		if (null != processCount && 0 == processCount.getBody().intValue()) {

			log.info("Finishing the batch for batchId -> {}", batchId);

			Long totalRecordsInBatch = coreConcurrentProcessLogController.countTotalRecordsInBatch(batchId).getBody();

			ScheduledBatchLogResponse scheduledBatchLogResponse = coreScheduledBatchLogController
					.updateBatch(batchId, totalRecordsInBatch).getBody();

			log.info(
					"Total Count of records completed in a batch {} is {} and totalRecordsInBatch from processLog table is {}",
					batchId, scheduledBatchLogResponse.getTotalRecords(), totalRecordsInBatch);
		} else {

			if (null != processCount) {

				log.warn("Total Count of process pending to be completed is {}", processCount.getBody());
			} else {
				log.error("Something went wrong, processCount cannot be null");
			}
		}

	}

	public boolean isBatchCompleted(String batchId) {

		log.info("Checking batch status for batchId -> {}", batchId);

		ScheduledBatchLogResponse scheduledBatchLogResponse = coreScheduledBatchLogController
				.findBatchByBatchId(batchId).getBody();

		return null != scheduledBatchLogResponse.getBatchEndDateTime();

	}

	public boolean isBatchCompletedWithFailure(String batchId) {

		log.info("Checking Failure status for batchId -> {}", batchId);

		List<String> batchIds = new ArrayList<String>(1);
		batchIds.add(batchId);

		Long totalFailureCount = coreProcessFailureLogController.countProcessFailuresByBatchIds(batchIds).getBody();

		return totalFailureCount != 0;

	}

	public void createFailureRecord(String accountId, String batchId, String failureCode, String failureReason,
			String failureStep, Throwable exp) {

		log.error(
				"Failure occurred for accountId -> {} and batchId {} with failureCode -> {}, failureReason -> {}, exceptionMessage is {} and cause is {}",
				accountId, batchId, failureCode, failureReason, exp.getMessage(), exp.getCause());

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode);
		concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		concurrentProcessFailureLogDefinition.setFailureDateTime(DateTimeUtil.currentTimeInString());
		concurrentProcessFailureLogDefinition.setFailureRecordId(accountId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep);
		concurrentProcessFailureLogDefinition.setBatchId(batchId);

		coreProcessFailureLogController.saveFailureLog(concurrentProcessFailureLogDefinition);
	}

	public BatchResultInformation finishBatchProcessData(ReportRunArgs apiRunArgs, String batchType, String batchId) {

		finishBatchProcess(batchId);

		BatchResultInformation batchResultInformation = new BatchResultInformation();
		batchResultInformation.setBatchId(batchId);

		if (apiRunArgs.isCompleteBatchOnError()
				|| (isBatchCompleted(batchId) && !isBatchCompletedWithFailure(batchId))) {

			if (isBatchCompletedWithFailure(batchId)) {

				publishBatchData(batchId, true, batchResultInformation);
				log.info(
						" ------------------------------ " + batchType
								+ " batch with batchId -> {} closed but with errors ------------------------------ ",
						batchId);

				return createBatchResultInfo(batchId, ValidationResult.SOMEORALLFAILED.toString(),
						batchResultInformation);
			} else {

				publishBatchData(batchId, false, batchResultInformation);
				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ " + batchType
								+ " batch with batchId -> {} completed successfully *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ",
						batchId);
				return createBatchResultInfo(batchId, ValidationResult.SUCCESS.toString(), batchResultInformation);
			}
		} else {

			publishBatchData(batchId, true, batchResultInformation);
			log.info(
					" ------------------------------ " + batchType
							+ " batch with batchId -> {} NOT Completed successfully ------------------------------ ",
					batchId);

			return createBatchResultInfo(batchId, ValidationResult.SOMEORALLFAILED.toString(), batchResultInformation);
		}
	}

	private BatchResultInformation createBatchResultInfo(String batchId, String batchStatus,
			BatchResultInformation batchResultInformation) {

		batchResultInformation.setBatchId(batchId);
		batchResultInformation.setBatchStatus(batchStatus);

		return batchResultInformation;
	}

	public boolean isBatchCreatedWithWorkerThreads(String batchId) {

		log.info("Checking Worker threads for batchId -> {}", batchId);

		try {

			Long totalPendingWorkerThreads = coreConcurrentProcessLogController
					.countPendingConcurrentProcessInBatch(batchId).getBody();

			Long totalCompletedWorkerThreads = coreConcurrentProcessLogController
					.countCompletedConcurrentProcessInBatch(batchId).getBody();

			log.info("Total Pending Worker thread for batchId -> {} is {} and total Completed Worker Threads is {}",
					batchId, totalPendingWorkerThreads, totalCompletedWorkerThreads);
			if (totalPendingWorkerThreads > 0 || totalCompletedWorkerThreads > 0) {

				return true;
			}

		} catch (ResourceNotFoundException exp) {

			log.warn("ResourceNotFoundException caught so no worker thread created");
			exp.printStackTrace();

		}

		return false;
	}
}