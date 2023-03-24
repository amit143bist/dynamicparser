package com.docusign.report.db.transformer;

import org.springframework.stereotype.Component;

import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.db.model.CoreConcurrentProcessLog;
import com.docusign.report.utils.DateTimeUtil;

@Component
public class ConcurrentProcessLogTransformer {

	public CoreConcurrentProcessLog tranformToCoreConcurrentProcessLog(
			ConcurrentProcessLogDefinition concurrentProcessLogRequest) {

		CoreConcurrentProcessLog coreConcurrentProcessLog = new CoreConcurrentProcessLog();
		coreConcurrentProcessLog.setBatchId(concurrentProcessLogRequest.getBatchId());
		coreConcurrentProcessLog.setProcessStatus(concurrentProcessLogRequest.getProcessStatus());
		coreConcurrentProcessLog.setTotalRecordsInProcess(concurrentProcessLogRequest.getTotalRecordsInProcess());
		coreConcurrentProcessLog.setProcessStartDateTime(DateTimeUtil.currentEpochTime());
		coreConcurrentProcessLog.setGroupId(concurrentProcessLogRequest.getGroupId());
		coreConcurrentProcessLog.setAccountId(concurrentProcessLogRequest.getAccountId());
		coreConcurrentProcessLog.setUserId(concurrentProcessLogRequest.getUserId());

		return coreConcurrentProcessLog;

	}

	public ConcurrentProcessLogDefinition tranformToConcurrentProcessLogResponse(
			CoreConcurrentProcessLog coreConcurrentProcessLog) {

		ConcurrentProcessLogDefinition concurrentProcessLogResponse = new ConcurrentProcessLogDefinition();
		concurrentProcessLogResponse.setBatchId(coreConcurrentProcessLog.getBatchId().toString());
		concurrentProcessLogResponse.setProcessId(coreConcurrentProcessLog.getProcessId().toString());
		concurrentProcessLogResponse.setProcessStartDateTime(coreConcurrentProcessLog.getProcessStartDateTime());
		concurrentProcessLogResponse.setProcessEndDateTime(coreConcurrentProcessLog.getProcessEndDateTime());
		concurrentProcessLogResponse.setProcessStatus(coreConcurrentProcessLog.getProcessStatus());
		concurrentProcessLogResponse.setTotalRecordsInProcess(coreConcurrentProcessLog.getTotalRecordsInProcess());
		concurrentProcessLogResponse.setGroupId(coreConcurrentProcessLog.getGroupId());
		concurrentProcessLogResponse.setAccountId(coreConcurrentProcessLog.getAccountId());
		concurrentProcessLogResponse.setUserId(coreConcurrentProcessLog.getUserId());

		return concurrentProcessLogResponse;
	}

}
