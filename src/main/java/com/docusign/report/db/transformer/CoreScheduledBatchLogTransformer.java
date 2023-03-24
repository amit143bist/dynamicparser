package com.docusign.report.db.transformer;

import org.springframework.stereotype.Component;

import com.docusign.report.db.domain.ScheduledBatchLogRequest;
import com.docusign.report.db.domain.ScheduledBatchLogResponse;
import com.docusign.report.db.model.CoreScheduledBatchLog;
import com.docusign.report.utils.DateTimeUtil;

@Component
public class CoreScheduledBatchLogTransformer {

	public CoreScheduledBatchLog transformToCoreScheduledBatchLog(ScheduledBatchLogRequest scheduledBatchLogRequest) {

		CoreScheduledBatchLog coreScheduledBatchLog = new CoreScheduledBatchLog();

		coreScheduledBatchLog.setBatchType(scheduledBatchLogRequest.getBatchType());
		coreScheduledBatchLog.setBatchStartParameters(scheduledBatchLogRequest.getBatchStartParameters());
		coreScheduledBatchLog.setBatchStartDateTime(DateTimeUtil.currentEpochTime());
		coreScheduledBatchLog.setTotalRecords(scheduledBatchLogRequest.getTotalRecords());

		return coreScheduledBatchLog;
	}

	public ScheduledBatchLogResponse transformToScheduledBatchLogResponse(CoreScheduledBatchLog coreScheduledBatchLog) {

		ScheduledBatchLogResponse scheduledBatchLogResponse = new ScheduledBatchLogResponse();

		scheduledBatchLogResponse.setBatchId(coreScheduledBatchLog.getBatchId().toString());
		scheduledBatchLogResponse.setBatchStartDateTime(coreScheduledBatchLog.getBatchStartDateTime());
		scheduledBatchLogResponse.setBatchEndDateTime(coreScheduledBatchLog.getBatchEndDateTime());
		scheduledBatchLogResponse.setBatchStartParameters(coreScheduledBatchLog.getBatchStartParameters());
		scheduledBatchLogResponse.setBatchType(coreScheduledBatchLog.getBatchType());
		scheduledBatchLogResponse.setTotalRecords(coreScheduledBatchLog.getTotalRecords());

		return scheduledBatchLogResponse;
	}
}