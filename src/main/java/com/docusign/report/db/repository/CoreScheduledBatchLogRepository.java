package com.docusign.report.db.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.docusign.report.db.model.CoreScheduledBatchLog;

@Repository(value = "batchLogRepository")
public interface CoreScheduledBatchLogRepository extends CrudRepository<CoreScheduledBatchLog, String> {

	Iterable<CoreScheduledBatchLog> findAllByBatchTypeAndBatchEndDateTimeIsNull(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchTypeAndBatchEndDateTimeIsNotNull(String batchType);

	Optional<CoreScheduledBatchLog> findTopByBatchTypeOrderByBatchStartDateTimeDesc(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchType(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchTypeAndBatchStartDateTimeBetween(String batchType, Long fromDate,
			Long toDate);
}