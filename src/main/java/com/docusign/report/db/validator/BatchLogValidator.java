package com.docusign.report.db.validator;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.docusign.report.db.domain.ScheduledBatchLogRequest;

@Service
public class BatchLogValidator implements IReportValidator<ScheduledBatchLogRequest> {

	@Override
	public void validateSaveData(ScheduledBatchLogRequest migrationInformation) {

		Assert.notNull(migrationInformation.getBatchType(), "BatchType cannot be null");
		Assert.notNull(migrationInformation.getBatchStartParameters(), "BatchStartParameters cannot be null");
	}

}