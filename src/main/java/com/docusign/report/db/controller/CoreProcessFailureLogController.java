package com.docusign.report.db.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.IterableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.docusign.report.common.constant.RetryStatus;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.db.domain.ConcurrentProcessFailureLogDefinition;
import com.docusign.report.db.domain.ConcurrentProcessFailureLogsInformation;
import com.docusign.report.db.model.CoreProcessFailureLog;
import com.docusign.report.db.repository.CoreProcessFailureLogRepository;
import com.docusign.report.db.transformer.CoreProcessFailureLogTransformer;
import com.docusign.report.db.validator.FailureLogValidator;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreProcessFailureLogController {

	@Autowired
	FailureLogValidator failureLogValidator;

	@Autowired
	CoreProcessFailureLogRepository coreProcessFailureLogRepository;

	@Autowired
	CoreProcessFailureLogTransformer coreProcessFailureLogTransformer;

	@PostMapping("/report/scheduledbatch/concurrentprocessfailure")
	public ResponseEntity<ConcurrentProcessFailureLogDefinition> saveFailureLog(
			@RequestBody ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest) {

		failureLogValidator.validateSaveData(concurrentProcessFailureLogRequest);
		CoreProcessFailureLog coreProcessFailureLog = coreProcessFailureLogTransformer
				.transformToCoreProcessFailureLog(concurrentProcessFailureLogRequest);

		log.debug("concurrentProcessFailureLogRequest transformed to coreProcessFailureLog for failureRecordId -> {}",
				concurrentProcessFailureLogRequest.getFailureRecordId());
		return Optional.ofNullable(coreProcessFailureLogRepository.save(coreProcessFailureLog)).map(failureLog -> {

			Assert.notNull(failureLog.getProcessFailureId(), "ProcessFailureId cannot be null for failureRecordId# "
					+ concurrentProcessFailureLogRequest.getFailureRecordId());

			return new ResponseEntity<ConcurrentProcessFailureLogDefinition>(
					coreProcessFailureLogTransformer.transformToConcurrentProcessFailureLogResponse(failureLog),
					HttpStatus.CREATED);
		}).orElseThrow(() -> new ResourceNotSavedException(
				"Failure Log not saved for " + concurrentProcessFailureLogRequest.getFailureRecordId()));
	}

	@PutMapping("/report/scheduledbatch/concurrentprocessfailure/processes/{processFailureId}")
	public ResponseEntity<ConcurrentProcessFailureLogDefinition> updateFailureLog(
			@RequestBody ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest,
			@PathVariable String processFailureId) {

		log.debug("updateFailureLog called for processFailureId -> {}", processFailureId);

		return coreProcessFailureLogRepository.findById(processFailureId).map(failureLog -> {

			coreProcessFailureLogTransformer.updateFailureLogData(concurrentProcessFailureLogRequest, failureLog);

			CoreProcessFailureLog savedCoreProcessFailureLog = coreProcessFailureLogRepository.save(failureLog);
			return new ResponseEntity<ConcurrentProcessFailureLogDefinition>(coreProcessFailureLogTransformer
					.transformToConcurrentProcessFailureLogResponse(savedCoreProcessFailureLog), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No ProcessFailure found with processFailureId# " + processFailureId));
	}

	@GetMapping("/report/scheduledbatch/concurrentprocessfailure")
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLog() {

		log.debug("listAllProcessFailureLog called");

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByRetryStatusOrRetryStatusIsNull(RetryStatus.F.toString());

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException("CoreProcessFailureLogList is empty or null");
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocessfailure/batch/{batchId}")
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForBatchId(
			@PathVariable String batchId) {

		log.debug("listAllProcessFailureLogForBatchId called for batchId -> {}", batchId);

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull(batchId, RetryStatus.F.toString(),
						batchId);

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException("CoreProcessFailureLogList is empty or null for batchId# " + batchId);
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocessfailure/failurerecords/{failureRecordId}")
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForFailureRecordId(
			@PathVariable String failureRecordId) {

		log.debug("listAllProcessFailureLogForFailureRecordId called for failureRecordId -> {}", failureRecordId);

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByFailureRecordIdAndRetryStatusOrFailureRecordIdAndRetryStatusIsNull(failureRecordId,
						RetryStatus.F.toString(), failureRecordId);

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException(
					"CoreProcessFailureLogList is empty or null for failurerecordId# " + failureRecordId);
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@PutMapping("/report/scheduledbatch/concurrentprocessfailure/failurerecords")
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllBatchFailuresByBatchIds(
			@RequestBody List<String> batchIds) {

		return prepareResponse(
				coreProcessFailureLogRepository.findAllByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(
						batchIds, RetryStatus.F.toString(), batchIds));
	}

	@PutMapping("/report/scheduledbatch/concurrentprocessfailure/failurerecords/batchids/count")
	public ResponseEntity<Long> countProcessFailuresByBatchIds(@RequestBody List<String> batchIds) {

		log.debug("countProcessFailuresByBatchIds called for batchIds -> {}", batchIds);

		return Optional.ofNullable(
				coreProcessFailureLogRepository.countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(batchIds,
						RetryStatus.F.toString(), batchIds))
				.map(failureCount -> {

					Assert.state(failureCount > -1, "ProcessCount should be greater than -1, check the processIds ");

					return new ResponseEntity<Long>(failureCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned"));
	}

	@GetMapping("/report/scheduledbatch/concurrentprocessfailure/failurerecords/count")
	public ResponseEntity<Long> countProcessFailures() {

		log.debug("countProcessFailures called");

		return Optional
				.ofNullable(
						coreProcessFailureLogRepository.countByRetryStatusOrRetryStatusIsNull(RetryStatus.F.toString()))
				.map(failureCount -> {

					Assert.state(failureCount > -1, "ProcessCount should be greater than -1 ");

					return new ResponseEntity<Long>(failureCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned"));
	}

	private ResponseEntity<ConcurrentProcessFailureLogsInformation> prepareResponse(
			Iterable<CoreProcessFailureLog> coreProcessFailureLogList) {

		log.debug("prepareResponse called");

		List<ConcurrentProcessFailureLogDefinition> concurrentProcessFailureLogResponseList = new ArrayList<ConcurrentProcessFailureLogDefinition>();

		coreProcessFailureLogList.forEach(failureLog -> {

			concurrentProcessFailureLogResponseList
					.add(coreProcessFailureLogTransformer.transformToConcurrentProcessFailureLogResponse(failureLog));
		});

		ConcurrentProcessFailureLogsInformation concurrentProcessFailureLogsInformation = new ConcurrentProcessFailureLogsInformation();
		concurrentProcessFailureLogsInformation
				.setConcurrentProcessFailureLogDefinitions(concurrentProcessFailureLogResponseList);

		if (null != concurrentProcessFailureLogResponseList && !concurrentProcessFailureLogResponseList.isEmpty()) {

			concurrentProcessFailureLogsInformation
					.setTotalFailureCount(Long.valueOf(concurrentProcessFailureLogResponseList.size()));
		}

		return new ResponseEntity<ConcurrentProcessFailureLogsInformation>(concurrentProcessFailureLogsInformation,
				HttpStatus.OK);
	}
}