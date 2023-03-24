package com.docusign.report.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.docusign.report.db.domain.ConcurrentProcessFailureLogsInformation;
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.db.domain.ConcurrentProcessLogsInformation;
import com.docusign.report.db.domain.ScheduledBatchLogResponse;
import com.docusign.report.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocuSignOutputDecorator {

	@Autowired
	DocuSignShellHelper shellHelper;

	public void formatScheduledBatchLogResponseOutput(ScheduledBatchLogResponse scheduledBatchLogResponse) {

		log.info("BatchId is {}", scheduledBatchLogResponse.getBatchId());
		shellHelper.printSuccess(String.format("BatchId is %s", scheduledBatchLogResponse.getBatchId()));

		log.info("BatchStartDateTime is {}", DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
				scheduledBatchLogResponse.getBatchStartDateTime(), TimeZone.getDefault().getID()));
		shellHelper.printSuccess(
				String.format("BatchStartDateTime is %s", DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
						scheduledBatchLogResponse.getBatchStartDateTime(), TimeZone.getDefault().getID())));

		if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

			log.info("BatchEndDateTime is {}", DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
					scheduledBatchLogResponse.getBatchEndDateTime(), TimeZone.getDefault().getID()));
			shellHelper.printSuccess(
					String.format("BatchEndDateTime is %s", DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
							scheduledBatchLogResponse.getBatchEndDateTime(), TimeZone.getDefault().getID())));

			log.info("Total records processed by batchId -> {} is {}", scheduledBatchLogResponse.getBatchId(),
					scheduledBatchLogResponse.getTotalRecords());

			shellHelper.printSuccess(String.format("Total records processed by batchId -> %s is %s",
					scheduledBatchLogResponse.getBatchId(), scheduledBatchLogResponse.getTotalRecords()));
		} else {

			log.warn("BatchId -> {} is still in the running status", scheduledBatchLogResponse.getBatchId());

			shellHelper.printWarning(String.format("BatchId ->  %s is still in the running status",
					scheduledBatchLogResponse.getBatchId()));
		}
	}

	public void formatAllThreadData(ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch,
			String batchId) {

		if (null != concurrentProcessLogsInformationForBatch) {

			List<ConcurrentProcessLogDefinition> processList = concurrentProcessLogsInformationForBatch
					.getConcurrentProcessLogDefinitions();

			processList.forEach(processInfo -> {

				if (null != processInfo.getProcessEndDateTime()) {

					log.info(" :::::::::::::::::::::::::::::: Success thread details :::::::::::::::::::::::::::::: ");
					shellHelper.printSuccess(String.format(
							" :::::::::::::::::::::::::::::: Success thread details :::::::::::::::::::::::::::::: "));
					if (null != processInfo.getAccountId()) {

						log.info("AccountId -> {} is successfully completed using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);

						shellHelper.printSuccess(String.format(
								"AccountId -> %s is successfully completed using processId -> %s for batchId -> %s",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId));

					} else {

						log.info(
								"AccountId -> {} is successfully completed using processId -> {} for batchId -> {} and completed {} records",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId,
								processInfo.getTotalRecordsInProcess());

						shellHelper.printSuccess(String.format(
								"AccountId -> %s is successfully completed using processId -> %s for batchId -> %s and completed %s records",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId,
								processInfo.getTotalRecordsInProcess()));
					}

				} else {

					log.info(" :::::::::::::::::::::::::::::: Failed thread details :::::::::::::::::::::::::::::: ");
					shellHelper.printError(String.format(
							" :::::::::::::::::::::::::::::: Failed thread details :::::::::::::::::::::::::::::: "));

					if (null != processInfo.getAccountId()) {

						log.info(
								"AccountId -> {} is either unsuccessful or in-progress using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);

						shellHelper.printWarning(String.format(
								"AccountId -> %s is either unsuccessful or in-progress using processId -> %s for batchId -> %s",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId));
					} else {

						log.info(
								"AccountId -> {} is either unsuccessful or in-progress using processId -> {} for batchId -> {}",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId);
						shellHelper.printWarning(String.format(
								"AccountId -> %s is either unsuccessful or in-progress using processId -> %s for batchId -> %s",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId));
					}

				}
			});
		}
	}

	public List<ConcurrentProcessLogDefinition> printAllAccountStatus(
			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails, String batchId) {

		List<ConcurrentProcessLogDefinition> inCompleteAccountIdProcessList = new ArrayList<ConcurrentProcessLogDefinition>();
		if (null != concurrentProcessLogsInformationAccountDetails
				&& null != concurrentProcessLogsInformationAccountDetails.getConcurrentProcessLogDefinitions()) {

			List<ConcurrentProcessLogDefinition> processAccountInfoList = concurrentProcessLogsInformationAccountDetails
					.getConcurrentProcessLogDefinitions();

			log.info(" #################### Total Accounts processed for batchId -> {} are {} #################### ",
					batchId, processAccountInfoList.size());

			shellHelper.printInfo(String.format(
					" #################### Total Accounts processed for batchId -> %s are %s #################### ",
					batchId, processAccountInfoList.size()));

			processAccountInfoList.forEach(accountInfo -> {

				if (null != accountInfo.getProcessEndDateTime()) {

					log.info(
							" #################### AccountId -> {} has successfully processed {} records #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());

					shellHelper.printInfo(String.format(
							" #################### AccountId -> {} has successfully processed {} records #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess()));

				} else {

					log.error(" #################### AccountId -> {} has reported error #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());

					shellHelper.printError(String.format(
							" #################### AccountId -> {} has reported error #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess()));

					inCompleteAccountIdProcessList.add(accountInfo);
				}

			});

		}

		return inCompleteAccountIdProcessList;
	}

	public void printFailureData(ConcurrentProcessFailureLogsInformation concurrentProcessFailureLogsInformation,
			String batchId) {

		log.error("Printing failurerecords for batchId -> {}", batchId);
		shellHelper.printError(String.format("Printing failurerecords for batchId -> %s", batchId));

		concurrentProcessFailureLogsInformation.getConcurrentProcessFailureLogDefinitions().forEach(failureInfo -> {

			log.error("{} failed at step {} with failureCode {} and failureReason {} in processId -> {} at {}",
					failureInfo.getFailureRecordId(), failureInfo.getFailureStep(), failureInfo.getFailureCode(),
					failureInfo.getFailureReason(), failureInfo.getProcessFailureId(),
					failureInfo.getFailureDateTime());

			shellHelper.printError(String.format(
					"%s failed at step %s with failureCode %s and failureReason %s in processId -> %s at %s",
					failureInfo.getFailureRecordId(), failureInfo.getFailureStep(), failureInfo.getFailureCode(),
					failureInfo.getFailureReason(), failureInfo.getProcessFailureId(),
					failureInfo.getFailureDateTime()));

		});

	}
}