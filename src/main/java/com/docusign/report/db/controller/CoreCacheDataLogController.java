package com.docusign.report.db.controller;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.docusign.report.common.constant.ValidationResult;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.db.model.CoreCacheDataLog;
import com.docusign.report.db.repository.CoreCacheDataLogRepository;
import com.docusign.report.db.transformer.CoreCacheLogTransformer;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreCacheDataLogController {

	@Autowired
	CoreCacheLogTransformer coreCacheLogTransformer;

	@Autowired
	CoreCacheDataLogRepository coreCacheDataLogRepository;

	@PostMapping("/report/corecachelog/saveall")
	public ResponseEntity<String> saveAllCache(@RequestBody List<CacheLogInformation> cacheLogInformationList) {

		log.info("Size of cacheList is {}", cacheLogInformationList.size());

		List<CoreCacheDataLog> coreCacheDataLogList = coreCacheLogTransformer
				.transformToCoreCacheDataLogList(cacheLogInformationList);

		return Optional.ofNullable(coreCacheDataLogRepository.saveAll(coreCacheDataLogList))
				.map(savedcoreCacheDataLog -> {

					log.info("Saved CacheData successfully");
					return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException("Cache not saved"));

	}

	@PostMapping("/report/corecachelog")
	public ResponseEntity<CacheLogInformation> saveCache(@RequestBody CacheLogInformation cacheLogInformation) {

		log.info("CacheKey {} and cacheValue {} need to be saved", cacheLogInformation.getCacheKey(),
				cacheLogInformation.getCacheValue());

		CoreCacheDataLog coreCacheDataLog = coreCacheLogTransformer.transformToCoreCacheDataLog(cacheLogInformation);

		ReentrantLock lock = new ReentrantLock();
		lock.lock();

		try {

			try {

				CacheLogInformation savedCacheLogInformation = findByCacheKeyAndCacheValue(
						cacheLogInformation.getCacheKey(), cacheLogInformation.getCacheValue()).getBody();

				log.warn("in Try Multiple thread tried to save same cacheKey {} and cacheValue {} in the DB",
						savedCacheLogInformation.getCacheKey(), savedCacheLogInformation.getCacheValue());

				return new ResponseEntity<CacheLogInformation>(savedCacheLogInformation, HttpStatus.ALREADY_REPORTED);

			} catch (ResourceNotFoundException resourceNotFoundException) {

				return Optional.ofNullable(coreCacheDataLogRepository.save(coreCacheDataLog))
						.map(savedcoreCacheDataLog -> {

							Assert.notNull(savedcoreCacheDataLog.getCacheId(),
									"CacheId cannot be null for CacheKey " + cacheLogInformation.getCacheKey());

							return new ResponseEntity<CacheLogInformation>(
									coreCacheLogTransformer.transformToCacheLogInformation(savedcoreCacheDataLog),
									HttpStatus.CREATED);
						}).orElseThrow(() -> new ResourceNotSavedException(
								"Cache not saved for " + cacheLogInformation.getCacheKey()));
			}

		} catch (DataIntegrityViolationException exception) {

			if (exception.getMessage().contains("UNIQUE")) {

				log.warn("Multiple thread tried to save same cacheKey {} and cacheValue {} in the DB",
						cacheLogInformation.getCacheKey(), cacheLogInformation.getCacheValue());

				return new ResponseEntity<CacheLogInformation>(
						findByCacheKeyAndCacheValue(cacheLogInformation.getCacheKey(),
								cacheLogInformation.getCacheValue()).getBody(),
						HttpStatus.ALREADY_REPORTED);
			}

		} catch (Exception exp) {

			log.error("Lock error occurred {}, message is {}, saveCache for cacheKey# {}", exp.getCause(),
					exp.getMessage(), cacheLogInformation.getCacheKey());
			throw new ResourceNotSavedException(
					"Lock error occurred, saveCache for cacheKey# " + cacheLogInformation.getCacheKey());
		} finally {

			lock.unlock();
		}

		return null;
	}

	@PutMapping("/report/corecachelog/{cacheId}")
	public ResponseEntity<CacheLogInformation> updateCache(@PathVariable String cacheId,
			@RequestBody CacheLogInformation cacheLogInformation) {

		log.debug("updateCache called for cacheId -> {}", cacheId);

		return coreCacheDataLogRepository.findById(cacheId).map(cacheDataLog -> {

			cacheDataLog.setCacheKey(cacheLogInformation.getCacheKey());
			cacheDataLog.setCacheValue(cacheLogInformation.getCacheValue());

			return new ResponseEntity<CacheLogInformation>(coreCacheLogTransformer
					.transformToCacheLogInformation(coreCacheDataLogRepository.save(cacheDataLog)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheId# " + cacheId));
	}

	@GetMapping("/report/corecachelog/cachekey/{cacheKey}")
	public ResponseEntity<CacheLogInformation> findByCacheKey(@PathVariable String cacheKey) {

		log.debug("FindByCacheKey called for cacheKey -> {}", cacheKey);

		return new ResponseEntity<CacheLogInformation>(coreCacheLogTransformer
				.transformToCacheLogInformation(coreCacheDataLogRepository.findByCacheKey(cacheKey).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey))),
				HttpStatus.OK);
	}

	@GetMapping("/report/corecachelog/cachevalue/{cacheValue}")
	public ResponseEntity<CacheLogInformation> findByCacheValue(@PathVariable String cacheValue) {

		log.debug("FindByCacheValue called for cacheValue -> {}", cacheValue);

		return new ResponseEntity<CacheLogInformation>(coreCacheLogTransformer.transformToCacheLogInformation(
				coreCacheDataLogRepository.findByCacheValue(cacheValue).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheValue " + cacheValue))),
				HttpStatus.OK);
	}

	@GetMapping("/report/corecachelog/cachevalue/{cacheValue}/cachereference/{cacheReference}")
	public ResponseEntity<CacheLogInformation> findByCacheValueAndCacheReference(@PathVariable String cacheValue,
			@PathVariable String cacheReference) {

		log.debug("FindByCacheValueAndCacheReference called for cacheValue -> {} and cacheReference -> {}", cacheValue,
				cacheReference);

		return new ResponseEntity<CacheLogInformation>(
				coreCacheLogTransformer.transformToCacheLogInformation(coreCacheDataLogRepository
						.findByCacheValueAndCacheReference(cacheValue, cacheReference).map(cacheData -> {

							return cacheData;
						}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheValue "
								+ cacheValue + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@GetMapping("/report/corecachelog/cachekey/{cacheKey}/cachevalue/{cacheValue}")
	public ResponseEntity<CacheLogInformation> findByCacheKeyAndCacheValue(@PathVariable String cacheKey,
			@PathVariable String cacheValue) {

		log.debug("FindByCacheKeyAndCacheValue called for cacheKey -> {} and cacheValue -> {}", cacheKey, cacheValue);

		return new ResponseEntity<CacheLogInformation>(coreCacheLogTransformer.transformToCacheLogInformation(
				coreCacheDataLogRepository.findByCacheKeyAndCacheValue(cacheKey, cacheValue).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No Cache found with cacheKey " + cacheKey + " cacheValue -> " + cacheValue))),
				HttpStatus.OK);
	}

	@GetMapping("/report/corecachelog/cachekey/{cacheKey}/cachereference/{cacheReference}")
	public ResponseEntity<CacheLogInformation> findByCacheKeyAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheReference) {

		log.debug("FindByCacheKeyAndCacheReference called for cacheKey -> {} and cacheReference -> {}", cacheKey,
				cacheReference);

		return new ResponseEntity<CacheLogInformation>(coreCacheLogTransformer.transformToCacheLogInformation(
				coreCacheDataLogRepository.findByCacheKeyAndCacheReference(cacheKey, cacheReference).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No Cache found with cacheKey " + cacheKey + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@GetMapping("/report/corecachelog/cachekey/{cacheKey}/cachevalue/{cacheValue}/cachereference/{cacheReference}")
	public ResponseEntity<CacheLogInformation> findByCacheKeyValueAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheValue, @PathVariable String cacheReference) {

		log.debug(
				"FindByCacheKeyValueAndCacheReference called for cacheKey -> {}, cacheValue -> {} and cacheReference -> {}",
				cacheKey, cacheValue, cacheReference);

		return new ResponseEntity<CacheLogInformation>(
				coreCacheLogTransformer.transformToCacheLogInformation(coreCacheDataLogRepository
						.findByCacheKeyAndCacheValueAndCacheReference(cacheKey, cacheValue, cacheReference)
						.map(cacheData -> {

							return cacheData;
						})
						.orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey -> " + cacheKey
								+ " cacheValue -> " + cacheValue + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@DeleteMapping("/report/corecachelog/cachekey/{cacheKey}")
	public ResponseEntity<String> deleteByCacheKey(@PathVariable String cacheKey) {

		log.debug("DeleteByCacheKey called for cacheKey -> {}", cacheKey);

		return coreCacheDataLogRepository.findByCacheKey(cacheKey).map(cacheData -> {

			coreCacheDataLogRepository.delete(cacheData);

			return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.ACCEPTED);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey));
	}

	@DeleteMapping("/report/corecachelog/cachekey/{cacheKey}/cachereference/{cacheReference}")
	public ResponseEntity<String> deleteByCacheKeyAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheReference) {

		log.debug("DeleteByCacheKeyAndCacheReference called for cacheKey -> {} and cacheReference -> {}", cacheKey,
				cacheReference);

		return coreCacheDataLogRepository.findByCacheKeyAndCacheReference(cacheKey, cacheReference).map(cacheData -> {

			coreCacheDataLogRepository.delete(cacheData);
			return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.ACCEPTED);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey));
	}
}