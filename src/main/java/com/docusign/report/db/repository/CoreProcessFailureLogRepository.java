package com.docusign.report.db.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.docusign.report.db.model.CoreProcessFailureLog;

@Repository(value = "coreProcessFailureLogRepository")
public interface CoreProcessFailureLogRepository extends CrudRepository<CoreProcessFailureLog, String> {

	Iterable<CoreProcessFailureLog> findAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull(String batchId,
			String retryStatus, String orBatchId);

	Iterable<CoreProcessFailureLog> findAllByFailureRecordIdAndRetryStatusOrFailureRecordIdAndRetryStatusIsNull(
			String failureRecordId, String retryStatus, String orfailureRecordId);

	Iterable<CoreProcessFailureLog> findAllByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(
			List<String> batchIds, String retryStatus, List<String> orbatchIds);

	Long countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(List<String> batchIds, String retryStatus,
			List<String> orBatchIds);

	Iterable<CoreProcessFailureLog> findAllByRetryStatusOrRetryStatusIsNull(String retryStatus);

	Long countByRetryStatusOrRetryStatusIsNull(String retryStatus);
}