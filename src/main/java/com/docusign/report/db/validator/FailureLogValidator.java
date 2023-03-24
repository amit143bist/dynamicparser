package com.docusign.report.db.validator;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.docusign.report.db.domain.ConcurrentProcessFailureLogDefinition;

@Service
public class FailureLogValidator implements IReportValidator<ConcurrentProcessFailureLogDefinition> {

	@Override
	public void validateSaveData(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest) {

		Assert.notNull(concurrentProcessFailureLogRequest.getBatchId(), "BatchId cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureCode(), "FailureCode cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureReason(), "FailureReason cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureDateTime(), "FailureDateTime cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureRecordId(), "FailureRecordId cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureStep(), "FailureStep cannot be null");
	}

}