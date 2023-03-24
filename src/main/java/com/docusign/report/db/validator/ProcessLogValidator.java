package com.docusign.report.db.validator;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;

@Service
public class ProcessLogValidator implements IReportValidator<ConcurrentProcessLogDefinition> {

	@Override
	public void validateSaveData(ConcurrentProcessLogDefinition concurrentProcessLogRequest) {

		Assert.notNull(concurrentProcessLogRequest.getBatchId(), "BatchId cannot be null");
		Assert.notNull(concurrentProcessLogRequest.getProcessStatus(), "ProcessStatus cannot be null");
		Assert.notNull(concurrentProcessLogRequest.getTotalRecordsInProcess(), "TotalRecordsInProcess cannot be null");
	}

}