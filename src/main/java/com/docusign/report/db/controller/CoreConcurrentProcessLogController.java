package com.docusign.report.db.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

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
import com.docusign.report.db.domain.ConcurrentProcessLogDefinition;
import com.docusign.report.db.domain.ConcurrentProcessLogsInformation;
import com.docusign.report.db.model.CoreConcurrentProcessLog;
import com.docusign.report.db.repository.CoreConcurrentProcessLogRepository;
import com.docusign.report.db.transformer.ConcurrentProcessLogTransformer;
import com.docusign.report.db.validator.ProcessLogValidator;
import com.docusign.report.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreConcurrentProcessLogController {

	@Autowired
	ProcessLogValidator processLogValidator;

	@Autowired
	ConcurrentProcessLogTransformer concurrentProcessLogTransformer;

	@Autowired
	CoreConcurrentProcessLogRepository concurrentProcessLogRepository;

	ReentrantLock lock = new ReentrantLock();

	@PostMapping("/report/scheduledbatch/concurrentprocess")
	public ResponseEntity<ConcurrentProcessLogDefinition> saveConcurrentProcess(
			@RequestBody ConcurrentProcessLogDefinition concurrentProcessLogRequest) {

		processLogValidator.validateSaveData(concurrentProcessLogRequest);
		CoreConcurrentProcessLog coreConcurrentProcessLog = concurrentProcessLogTransformer
				.tranformToCoreConcurrentProcessLog(concurrentProcessLogRequest);

		log.debug("concurrentProcessLogRequest transformed to coreConcurrentProcessLog for batchId -> {}",
				concurrentProcessLogRequest.getBatchId());
		return Optional.ofNullable(concurrentProcessLogRepository.save(coreConcurrentProcessLog))
				.map(savedCoreConcurrentProcessLog -> {

					Assert.notNull(savedCoreConcurrentProcessLog.getProcessId(),
							"ProcessId cannot be null for batchId# " + concurrentProcessLogRequest.getBatchId());

					log.debug(
							"savedCoreConcurrentProcessLog successfully saved in CoreConcurrentProcessLogController.saveConcurrentProcess()");
					return new ResponseEntity<ConcurrentProcessLogDefinition>(concurrentProcessLogTransformer
							.tranformToConcurrentProcessLogResponse(savedCoreConcurrentProcessLog), HttpStatus.CREATED);
				})
				.orElseThrow(() -> new ResourceNotSavedException("Requested Concurrent Process not saved for batchId# "
						+ concurrentProcessLogRequest.getBatchId()));
	}

	@PutMapping("/report/scheduledbatch/concurrentprocess/processes/{processId}")
	public ResponseEntity<ConcurrentProcessLogDefinition> updateConcurrentProcess(
			@RequestBody ConcurrentProcessLogDefinition concurrentProcessLogDefinition,
			@PathVariable String processId) {

		log.debug("updateConcurrentProcess called for processId -> {}", processId);

		return concurrentProcessLogRepository.findById(processId).map(coreConcurrentProcess -> {

			if (null != coreConcurrentProcess.getProcessEndDateTime()) {

				log.warn("ProcessEndDateTime should be null for processId-> {}", processId);
				return new ResponseEntity<ConcurrentProcessLogDefinition>(
						concurrentProcessLogTransformer.tranformToConcurrentProcessLogResponse(coreConcurrentProcess),
						HttpStatus.ALREADY_REPORTED);

			}

			coreConcurrentProcess.setProcessEndDateTime(DateTimeUtil.currentEpochTime());
			coreConcurrentProcess.setProcessStatus(concurrentProcessLogDefinition.getProcessStatus());

			if (null != concurrentProcessLogDefinition.getTotalRecordsInProcess()) {

				coreConcurrentProcess
						.setTotalRecordsInProcess(concurrentProcessLogDefinition.getTotalRecordsInProcess());
			}

			CoreConcurrentProcessLog savedCoreConcurrentProcessLog = null;

			ReentrantLock lock = new ReentrantLock();

			lock.lock();
			try {

				savedCoreConcurrentProcessLog = concurrentProcessLogRepository.save(coreConcurrentProcess);
			} catch (Exception exp) {

				log.error(
						"Lock error occurred {}, message is {}, requested Concurrent Process not saved for batchId# {}",
						exp.getCause(), exp.getMessage(), concurrentProcessLogDefinition.getBatchId());
				throw new ResourceNotSavedException(
						"Lock error occurred, requested Concurrent Process not saved for batchId# "
								+ concurrentProcessLogDefinition.getBatchId());
			} finally {

				lock.unlock();
			}

			return new ResponseEntity<ConcurrentProcessLogDefinition>(concurrentProcessLogTransformer
					.tranformToConcurrentProcessLogResponse(savedCoreConcurrentProcessLog), HttpStatus.OK);

		}).orElseThrow(() -> new ResourceNotFoundException("No Process found with processId# " + processId));
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/successparentgroup/{batchId}")
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllSuccessParentGroups(@PathVariable String batchId) {

		return prepareResponse(concurrentProcessLogRepository
				.findAllByBatchIdAndAccountIdIsNullAndProcessEndDateTimeIsNotNull(batchId), batchId);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/allparentgroup/{batchId}")
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllParentGroups(@PathVariable String batchId) {

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndAccountIdIsNull(batchId), batchId);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/totalrecords/{batchId}")
	public ResponseEntity<Long> countTotalRecordsInBatch(@PathVariable String batchId) {

		return Optional.ofNullable(concurrentProcessLogRepository.getTotalRecordsInBatch(batchId))
				.map(totalRecordsCount -> {

					Assert.state(totalRecordsCount > -1,
							"TotalRecordsInBatch should be greater than -1, check the batchId# " + batchId);

					return new ResponseEntity<Long>(totalRecordsCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No result returned in countTotalRecordsInBatch for batchId# " + batchId));
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/totalrecords/group/{groupId}")
	public ResponseEntity<Long> countTotalRecordsInGroup(@PathVariable String groupId) {

		return Optional.ofNullable(concurrentProcessLogRepository.getTotalRecordsInGroup(groupId))
				.map(totalRecordsCount -> {

					Assert.state(totalRecordsCount > -1,
							"TotalRecordsInBatch should be greater than -1, check the groupId# " + groupId);

					return new ResponseEntity<Long>(totalRecordsCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No result returned in countTotalRecordsInBatch for groupId# " + groupId));
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/countpendingprocesses/{batchId}")
	public ResponseEntity<Long> countPendingConcurrentProcessInBatch(@PathVariable String batchId) {

		log.debug("countPendingConcurrentProcessInBatch called for batchId -> {}", batchId);

		lock.lock();
		try {
			return Optional
					.ofNullable(concurrentProcessLogRepository.countByBatchIdAndProcessEndDateTimeIsNull(batchId))
					.map(processCount -> {

						Assert.state(processCount > -1,
								"ProcessCount should be greater than -1, check the batchId# " + batchId);

						log.info("Pending processes for batchId -> {} is {}", batchId, processCount);
						return new ResponseEntity<Long>(processCount, HttpStatus.OK);
					}).orElseThrow(() -> new ResourceNotFoundException("No result returned for batchId# " + batchId));
		} catch (Exception exp) {

			log.error(
					"Lock error occurred {}, message is {}, requested count of pending concurrentprocess for batchId# {}",
					exp.getCause(), exp.getMessage(), batchId);
			throw new ResourceNotSavedException(
					"Lock error occurred, requested count of pending concurrentprocess for batchId# " + batchId);
		} finally {

			lock.unlock();
		}
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/countcompletedprocesses/{batchId}")
	public ResponseEntity<Long> countCompletedConcurrentProcessInBatch(@PathVariable String batchId) {

		log.debug("countCompletedConcurrentProcessInBatch called for batchId -> {}", batchId);

		try {
			return Optional
					.ofNullable(concurrentProcessLogRepository.countByBatchIdAndProcessEndDateTimeIsNotNull(batchId))
					.map(processCount -> {

						log.info("Completed processes for batchId -> {} is {}", batchId, processCount);
						return new ResponseEntity<Long>(processCount, HttpStatus.OK);
					}).orElseThrow(() -> new ResourceNotFoundException("No result returned for batchId# " + batchId));
		} catch (Exception exp) {

			log.error(
					"Error occurred {}, message is {}, requested count of completed concurrentprocess for batchId# {}",
					exp.getCause(), exp.getMessage(), batchId);
			throw new ResourceNotSavedException(
					"Error occurred, requested count of completed concurrentprocess for batchId# " + batchId);
		}
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/processes/{batchId}")
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllProcessesForBatchId(@PathVariable String batchId) {

		log.debug("findAllBatchesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchId(batchId), batchId);

	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/incompleteprocesses/{batchId}")
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllInCompleteProcessesForBatchId(
			@PathVariable String batchId) {

		log.debug("findAllIncompleteBatchesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndProcessEndDateTimeIsNull(batchId),
				batchId);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/completeprocesses/{batchId}")
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllCompleteProcessesForBatchId(
			@PathVariable String batchId) {

		log.debug("findAllCompleteProcessesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndProcessEndDateTimeIsNotNull(batchId),
				batchId);
	}

	@GetMapping("/report/scheduledbatch/concurrentprocess/process/{processId}")
	public ResponseEntity<ConcurrentProcessLogDefinition> findProcessByProcessId(@PathVariable String processId) {

		log.debug("findProcessByProcessId called for processId -> {}", processId);

		return concurrentProcessLogRepository.findById(processId).map(processLog -> {

			return new ResponseEntity<ConcurrentProcessLogDefinition>(
					concurrentProcessLogTransformer.tranformToConcurrentProcessLogResponse(processLog), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"Requested Concurrent Process not found for processId# " + processId));
	}

	private ResponseEntity<ConcurrentProcessLogsInformation> prepareResponse(
			Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogIterable, String batchId) {

		log.debug("prepareResponse called for batchId -> {}", batchId);

		if (IterableUtil.isNullOrEmpty(coreConcurrentProcessLogIterable)) {

			if (null != batchId) {

				log.error("CoreConcurrentProcessLogIterable is null, No process exists for batchId# {}", batchId);
				throw new ResourceNotFoundException("No process exists for batchId# " + batchId);
			}
		}

		List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList = new ArrayList<ConcurrentProcessLogDefinition>();
		coreConcurrentProcessLogIterable.forEach(coreConcurrentProcessLog -> {

			concurrentProcessLogDefinitionList.add(
					concurrentProcessLogTransformer.tranformToConcurrentProcessLogResponse(coreConcurrentProcessLog));
		});

		ConcurrentProcessLogsInformation concurrentProcessLogsInformation = new ConcurrentProcessLogsInformation();
		concurrentProcessLogsInformation.setConcurrentProcessLogDefinitions(concurrentProcessLogDefinitionList);

		if (null != concurrentProcessLogDefinitionList && !concurrentProcessLogDefinitionList.isEmpty()) {

			concurrentProcessLogsInformation
					.setTotalProcessesCount(Long.valueOf(concurrentProcessLogDefinitionList.size()));
		}

		return new ResponseEntity<ConcurrentProcessLogsInformation>(concurrentProcessLogsInformation, HttpStatus.OK);
	}

}