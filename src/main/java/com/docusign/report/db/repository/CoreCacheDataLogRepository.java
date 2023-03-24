package com.docusign.report.db.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.docusign.report.db.model.CoreCacheDataLog;

@Repository(value = "coreCacheDataLogRepository")
public interface CoreCacheDataLogRepository extends CrudRepository<CoreCacheDataLog, String> {

	Optional<CoreCacheDataLog> findByCacheKey(String cacheKey);

	Optional<CoreCacheDataLog> findByCacheValue(String cacheValue);

	Optional<CoreCacheDataLog> findByCacheKeyAndCacheValue(String cacheKey, String cacheValue);

	Optional<CoreCacheDataLog> findByCacheKeyAndCacheValueAndCacheReference(String cacheKey, String cacheValue,
			String cacheReference);

	Optional<CoreCacheDataLog> findByCacheKeyAndCacheReference(String cacheKey, String cacheReference);

	Optional<CoreCacheDataLog> findByCacheValueAndCacheReference(String cacheValue, String cacheReference);

	void deleteByCacheKey(String cacheKey);
}