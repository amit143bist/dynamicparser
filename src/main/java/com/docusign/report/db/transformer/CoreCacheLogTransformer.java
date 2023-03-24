package com.docusign.report.db.transformer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.db.model.CoreCacheDataLog;

@Service
public class CoreCacheLogTransformer {

	public List<CoreCacheDataLog> transformToCoreCacheDataLogList(List<CacheLogInformation> cacheLogInformationList) {

		List<CoreCacheDataLog> coreCacheDataLogList = new ArrayList<CoreCacheDataLog>(cacheLogInformationList.size());

		cacheLogInformationList.forEach(cacheLogInformation -> {

			CoreCacheDataLog coreCacheDataLog = new CoreCacheDataLog();

			coreCacheDataLog.setCacheKey(cacheLogInformation.getCacheKey());
			coreCacheDataLog.setCacheValue(cacheLogInformation.getCacheValue());
			coreCacheDataLog.setCacheReference(cacheLogInformation.getCacheReference());

			coreCacheDataLogList.add(coreCacheDataLog);
		});

		return coreCacheDataLogList;
	}

	public CoreCacheDataLog transformToCoreCacheDataLog(CacheLogInformation cacheLogInformation) {

		CoreCacheDataLog coreCacheDataLog = new CoreCacheDataLog();

		coreCacheDataLog.setCacheKey(cacheLogInformation.getCacheKey());
		coreCacheDataLog.setCacheValue(cacheLogInformation.getCacheValue());
		coreCacheDataLog.setCacheReference(cacheLogInformation.getCacheReference());

		return coreCacheDataLog;
	}

	public CacheLogInformation transformToCacheLogInformation(CoreCacheDataLog coreCacheDataLog) {

		CacheLogInformation cacheLogInformation = new CacheLogInformation();

		cacheLogInformation.setCacheKey(coreCacheDataLog.getCacheKey());
		cacheLogInformation.setCacheValue(coreCacheDataLog.getCacheValue());
		cacheLogInformation.setCacheId(coreCacheDataLog.getCacheId());
		cacheLogInformation.setCacheReference(coreCacheDataLog.getCacheReference());
		cacheLogInformation.setProcessFlag("true");

		return cacheLogInformation;
	}
}