package com.docusign.report.db.transformer;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.docusign.report.common.constant.RetryStatus;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.db.domain.ConcurrentProcessFailureLogDefinition;
import com.docusign.report.db.model.CoreProcessFailureLog;
import com.docusign.report.utils.DateTimeUtil;

@Component
public class CoreProcessFailureLogTransformer {

	public CoreProcessFailureLog transformToCoreProcessFailureLog(
			ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest) {

		CoreProcessFailureLog coreProcessFailureLog = new CoreProcessFailureLog();

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureRecordId(), "FailureRecordId cannot be null");

		coreProcessFailureLog.setFailureRecordId(concurrentProcessFailureLogRequest.getFailureRecordId());

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureCode(), "FailureCode cannot be null");
		coreProcessFailureLog.setFailureCode(concurrentProcessFailureLogRequest.getFailureCode());

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureDateTime(), "FailureDateTime cannot be null");
		coreProcessFailureLog.setFailureDateTime(DateTimeUtil
				.convertToEpochTimeFromDateTimeAsString(concurrentProcessFailureLogRequest.getFailureDateTime()));

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureReason(), "FailureReason cannot be null");
		coreProcessFailureLog.setFailureReason(concurrentProcessFailureLogRequest.getFailureReason());
		coreProcessFailureLog.setBatchId(concurrentProcessFailureLogRequest.getBatchId());

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureStep(), "FailureStep cannot be null");
		coreProcessFailureLog.setFailureStep(concurrentProcessFailureLogRequest.getFailureStep());

		coreProcessFailureLog.setRetryStatus(concurrentProcessFailureLogRequest.getRetryStatus());
		coreProcessFailureLog.setRetryCount(concurrentProcessFailureLogRequest.getRetryCount());

		return coreProcessFailureLog;
	}

	public ConcurrentProcessFailureLogDefinition transformToConcurrentProcessFailureLogResponse(
			CoreProcessFailureLog coreProcessFailureLog) {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogResponse = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogResponse.setProcessFailureId(coreProcessFailureLog.getProcessFailureId().toString());
		concurrentProcessFailureLogResponse.setFailureRecordId(coreProcessFailureLog.getFailureRecordId());
		concurrentProcessFailureLogResponse.setFailureCode(coreProcessFailureLog.getFailureCode());
		concurrentProcessFailureLogResponse.setFailureDateTime(
				DateTimeUtil.convertToStringFromEpochTimeInSecs(coreProcessFailureLog.getFailureDateTime()));
		concurrentProcessFailureLogResponse.setFailureReason(coreProcessFailureLog.getFailureReason());
		concurrentProcessFailureLogResponse.setBatchId(coreProcessFailureLog.getBatchId().toString());
		concurrentProcessFailureLogResponse.setFailureStep(coreProcessFailureLog.getFailureStep());

		Optional.ofNullable(coreProcessFailureLog.getSuccessDateTime()).map(mapper -> {

			concurrentProcessFailureLogResponse.setSuccessDateTime(
					DateTimeUtil.convertToStringFromEpochTimeInSecs(coreProcessFailureLog.getSuccessDateTime()));
			return concurrentProcessFailureLogResponse;
		});

		concurrentProcessFailureLogResponse.setRetryStatus(coreProcessFailureLog.getRetryStatus());
		concurrentProcessFailureLogResponse.setRetryCount(coreProcessFailureLog.getRetryCount());

		return concurrentProcessFailureLogResponse;
	}

	public void updateFailureLogData(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest,
			CoreProcessFailureLog failureLog) {

		failureLog.setRetryStatus(concurrentProcessFailureLogRequest.getRetryStatus());

		if (null != failureLog.getRetryCount()) {

			failureLog.setRetryCount(Long.valueOf(failureLog.getRetryCount().intValue() + 1));
		} else {

			failureLog.setRetryCount(Long.valueOf(1));
		}

		Optional.ofNullable(concurrentProcessFailureLogRequest.getFailureCode()).map(mapper -> {

			failureLog.setFailureCode(concurrentProcessFailureLogRequest.getFailureCode());
			return failureLog;
		});

		Optional.ofNullable(concurrentProcessFailureLogRequest.getFailureReason()).map(mapper -> {

			failureLog.setFailureReason(concurrentProcessFailureLogRequest.getFailureReason());
			return failureLog;
		});

		if (null != concurrentProcessFailureLogRequest.getRetryStatus()
				&& RetryStatus.F.toString().equalsIgnoreCase(concurrentProcessFailureLogRequest.getRetryStatus())) {

			Optional.ofNullable(concurrentProcessFailureLogRequest.getFailureDateTime()).map(mapper -> {

				failureLog.setFailureDateTime(DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
						concurrentProcessFailureLogRequest.getFailureDateTime()));
				return failureLog;
			}).orElseThrow(() -> new InvalidInputException(
					"FailureDateTime is null for processFailureId# " + failureLog.getProcessFailureId()));

		}

		if (null != concurrentProcessFailureLogRequest.getRetryStatus()
				&& RetryStatus.S.toString().equalsIgnoreCase(concurrentProcessFailureLogRequest.getRetryStatus())) {

			Optional.ofNullable(concurrentProcessFailureLogRequest.getSuccessDateTime()).map(mapper -> {

				failureLog.setSuccessDateTime(DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
						concurrentProcessFailureLogRequest.getSuccessDateTime()));
				return failureLog;
			}).orElseThrow(() -> new InvalidInputException(
					"SuccessDateTime is null for processFailureId# " + failureLog.getProcessFailureId()));

		}

	}
}