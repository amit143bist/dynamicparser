package com.docusign.report.db.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.docusign.report.db.model.CoreConcurrentProcessLog;

@Repository(value = "concurrentProcessLogRepository")
public interface CoreConcurrentProcessLogRepository extends CrudRepository<CoreConcurrentProcessLog, String> {

	Long countByBatchIdAndProcessEndDateTimeIsNull(String batchId);
	
	Long countByBatchIdAndProcessEndDateTimeIsNotNull(String batchId);

	Iterable<CoreConcurrentProcessLog> findAllByBatchId(String batchId);

	Iterable<CoreConcurrentProcessLog> findAllByBatchIdAndProcessEndDateTimeIsNull(String batchId);

	Iterable<CoreConcurrentProcessLog> findAllByBatchIdAndProcessEndDateTimeIsNotNull(String batchId);

	Iterable<CoreConcurrentProcessLog> findAllByBatchIdAndAccountIdIsNullAndProcessEndDateTimeIsNotNull(String batchId);
	
	Iterable<CoreConcurrentProcessLog> findAllByBatchIdAndAccountIdIsNull(String batchId);

	@Query(value = "SELECT sum(totalRecordsInProcess) from coreconcurrentprocesslog where batchId = ?1 and accountId is null", nativeQuery = true)
	Long getTotalRecordsInBatch(String batchId);

	@Query(value = "SELECT sum(totalRecordsInProcess) from coreconcurrentprocesslog where groupId = ?1", nativeQuery = true)
	Long getTotalRecordsInGroup(String groupId);

}