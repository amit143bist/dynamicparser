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

import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.common.exception.RunningBatchException;
import com.docusign.report.db.domain.ScheduledBatchLogRequest;
import com.docusign.report.db.domain.ScheduledBatchLogResponse;
import com.docusign.report.db.domain.ScheduledBatchLogsInformation;
import com.docusign.report.db.model.CoreScheduledBatchLog;
import com.docusign.report.db.repository.CoreScheduledBatchLogRepository;
import com.docusign.report.db.transformer.CoreScheduledBatchLogTransformer;
import com.docusign.report.db.validator.BatchLogValidator;
import com.docusign.report.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreScheduledBatchLogController {

	@Autowired
	BatchLogValidator batchLogValidator;

	@Autowired
	CoreScheduledBatchLogRepository batchLogRepository;

	@Autowired
	CoreScheduledBatchLogTransformer coreScheduledBatchLogTransformer;

	@PostMapping("/report/scheduledbatch")
	public ResponseEntity<ScheduledBatchLogResponse> saveBatch(
			@RequestBody ScheduledBatchLogRequest scheduledBatchLogRequest) {

		batchLogValidator.validateSaveData(scheduledBatchLogRequest);
		CoreScheduledBatchLog coreScheduledBatchLog = coreScheduledBatchLogTransformer
				.transformToCoreScheduledBatchLog(scheduledBatchLogRequest);

		log.debug("scheduledBatchLogRequest transformed to coreScheduledBatchLog for batchType -> {}",
				scheduledBatchLogRequest.getBatchType());
		return Optional.ofNullable(batchLogRepository.save(coreScheduledBatchLog)).map(savedCoreScheduledBatchLog -> {

			Assert.notNull(savedCoreScheduledBatchLog.getBatchId(),
					"BatchId cannot be null for batchType " + scheduledBatchLogRequest.getBatchType());

			return new ResponseEntity<ScheduledBatchLogResponse>(
					coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(savedCoreScheduledBatchLog),
					HttpStatus.CREATED);
		}).orElseThrow(
				() -> new ResourceNotSavedException("Batch not saved for " + scheduledBatchLogRequest.getBatchType()));
	}

	@PutMapping("/report/scheduledbatch/{batchId}")
	public ResponseEntity<ScheduledBatchLogResponse> updateBatch(@PathVariable String batchId) {

		log.info("updateBatch called for batchId -> {}", batchId);

		return batchLogRepository.findById(batchId).map(scheduledBatch -> {

			if (null != scheduledBatch.getBatchEndDateTime()) {

				log.warn("BatchEndDateTime should be null for batchId#-> {}", batchId);

				return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
						.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)),
						HttpStatus.ALREADY_REPORTED);

			}

			scheduledBatch.setBatchEndDateTime(DateTimeUtil.currentEpochTime());

			return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
					.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Batch found with batchId# " + batchId));
	}

	@PutMapping("/report/scheduledbatch/{batchId}/totalrecordsinbatch/{totalRecordsInBatch}")
	public ResponseEntity<ScheduledBatchLogResponse> updateBatch(@PathVariable String batchId,
			@PathVariable Long totalRecordsInBatch) {

		log.info("updateBatch called for batchId -> {} and totalRecordsInBatch -> {}", batchId, totalRecordsInBatch);

		return batchLogRepository.findById(batchId).map(scheduledBatch -> {

			if (null != scheduledBatch.getBatchEndDateTime()) {

				log.warn("BatchEndDateTime should be null for batchId#-> {}", batchId);

				return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
						.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)),
						HttpStatus.ALREADY_REPORTED);

			}

			if (null != totalRecordsInBatch) {

				scheduledBatch.setTotalRecords(Long.valueOf(totalRecordsInBatch));
			}
			scheduledBatch.setBatchEndDateTime(DateTimeUtil.currentEpochTime());

			return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
					.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Batch found with batchId# " + batchId));
	}

	@GetMapping("/report/scheduledbatch/batchtype/{batchType}")
	public ResponseEntity<ScheduledBatchLogResponse> findInCompleteBatch(@PathVariable String batchType) {

		log.debug("findInCompleteBatch called for batchType -> {}", batchType);

		Iterable<CoreScheduledBatchLog> scheduledBatchLogList = batchLogRepository
				.findAllByBatchTypeAndBatchEndDateTimeIsNull(batchType);

		return Optional.ofNullable(scheduledBatchLogList).map(batchLogList -> {

			if (IterableUtil.isNullOrEmpty(batchLogList)) {

				throw new ResourceNotFoundException("No Batch running with batch type " + batchType);
			}

			if (IterableUtil.sizeOf(batchLogList) > 1) {

				throw new RunningBatchException("More than one batch is already running for batchType " + batchType);
			}

			CoreScheduledBatchLog batchLog = batchLogList.iterator().next();

			Assert.notNull(batchLog.getBatchId(), "BatchId cannot be null for batchType " + batchType);
			Assert.isNull(batchLog.getBatchEndDateTime(), "BatchEndDateTime should be null for batchType " + batchType);

			return new ResponseEntity<ScheduledBatchLogResponse>(
					coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(batchLog), HttpStatus.OK);

		}).orElseThrow(() -> new ResourceNotFoundException("No Batch running with batch type " + batchType));
	}

	@GetMapping("/report/scheduledbatch/latestbatch/batchid/{batchId}")
	public ResponseEntity<ScheduledBatchLogResponse> findBatchByBatchId(@PathVariable String batchId) {

		log.debug("findBatchByBatchId called for batchId -> {}", batchId);

		return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
				.transformToScheduledBatchLogResponse(batchLogRepository.findById(batchId).map(scheduledBatch -> {

					return scheduledBatch;
				}).orElseThrow(() -> new ResourceNotFoundException("No Batch available for batchId# " + batchId))),
				HttpStatus.OK);
	}

	@GetMapping("/report/scheduledbatch/latestbatch/batchtype/{batchType}")
	public ResponseEntity<ScheduledBatchLogResponse> findLatestBatchByBatchType(@PathVariable String batchType) {

		log.debug("findLatestBatchByBatchType called for batchType -> {}", batchType);

		return new ResponseEntity<ScheduledBatchLogResponse>(
				coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(batchLogRepository
						.findTopByBatchTypeOrderByBatchStartDateTimeDesc(batchType).map(scheduledBatch -> {

							return scheduledBatch;
						}).orElseThrow(
								() -> new ResourceNotFoundException("No Batch running with batch type " + batchType))),
				HttpStatus.OK);
	}

	@GetMapping("/report/scheduledbatch/incompletebatches/batchtype/{batchType}")
	public ResponseEntity<ScheduledBatchLogsInformation> findAllInCompleteBatches(@PathVariable String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchTypeAndBatchEndDateTimeIsNull(batchType), batchType);
	}

	@GetMapping("/report/scheduledbatch/batches/batchtype/{batchType}")
	public ResponseEntity<ScheduledBatchLogsInformation> findAllBatchesByBatchType(@PathVariable String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchType(batchType), batchType);
	}

	@GetMapping("/report/scheduledbatch/batches/batchtype/{batchType}/fromdate/{fromDate}/todate/{toDate}")
	public ResponseEntity<ScheduledBatchLogsInformation> findAllByBatchTypeAndBatchStartDateTimeBetween(
			@PathVariable String batchType, @PathVariable String fromDate, @PathVariable String toDate) {

		return prepareResponse(batchLogRepository.findAllByBatchTypeAndBatchStartDateTimeBetween(batchType,
				DateTimeUtil.convertToEpochTimeFromDateTimeAsString(fromDate + "T00:00:00.0000000Z"),
				DateTimeUtil.convertToEpochTimeFromDateTimeAsString(toDate + "T23:59:59.9999999Z")), batchType);
	}

	@GetMapping("/report/scheduledbatch/batches")
	public ResponseEntity<ScheduledBatchLogsInformation> findAllBatches() {

		return prepareResponse(batchLogRepository.findAll(), null);
	}

	@GetMapping("/report/scheduledbatch/completebatches/batchtype/{batchType}")
	public ResponseEntity<ScheduledBatchLogsInformation> findAllCompleteBatches(@PathVariable String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchTypeAndBatchEndDateTimeIsNotNull(batchType), batchType);
	}

	private ResponseEntity<ScheduledBatchLogsInformation> prepareResponse(
			Iterable<CoreScheduledBatchLog> coreScheduledBatchLogIterable, String batchType) {

		log.debug("prepareResponse is called for batchType -> {}", batchType);
		if (IterableUtil.isNullOrEmpty(coreScheduledBatchLogIterable)) {

			if (null != batchType) {
				throw new ResourceNotFoundException("No Batch exists for batchType " + batchType);
			} else {
				throw new ResourceNotFoundException("No Batch exists");
			}
		}

		List<ScheduledBatchLogResponse> scheduledBatchLogResponseList = new ArrayList<ScheduledBatchLogResponse>();
		coreScheduledBatchLogIterable.forEach(coreScheduledBatchLog -> {

			scheduledBatchLogResponseList
					.add(coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(coreScheduledBatchLog));
		});

		ScheduledBatchLogsInformation scheduledBatchLogsInformation = new ScheduledBatchLogsInformation();
		scheduledBatchLogsInformation.setScheduledBatchLogResponses(scheduledBatchLogResponseList);

		if (null != scheduledBatchLogResponseList && !scheduledBatchLogResponseList.isEmpty()) {

			scheduledBatchLogsInformation.setTotalBatchesCount(Long.valueOf(scheduledBatchLogResponseList.size()));
		}

		return new ResponseEntity<ScheduledBatchLogsInformation>(scheduledBatchLogsInformation, HttpStatus.OK);
	}

}