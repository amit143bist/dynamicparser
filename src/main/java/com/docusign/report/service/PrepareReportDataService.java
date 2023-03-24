package com.docusign.report.service;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.JWTParams;
import com.docusign.report.cache.DSAuthorizationCache;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AccountFetchAPITypes;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.domain.ApiHourlyLimitData;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.ReportRunArgs;
import com.docusign.report.dsapi.domain.AccountUser;
import com.docusign.report.dsapi.service.DSAccountService;
import com.docusign.report.dsapi.service.OrgAdminService;
import com.docusign.report.utils.DateTimeUtil;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrepareReportDataService {

	@Autowired
	OrgAdminService orgAdminService;

	@Autowired
	DSAccountService dsAccountService;

	@Autowired
	DSAuthorizationCache dsAuthorizationCache;

	@Value("${app.apiThresholdLimitPercent}")
	private Integer apiThresholdLimitPercent;

	public void validateReportRunArgs(ReportRunArgs apiRunArgs) {

		log.info(" #################### Validating the ReportRunArgs for loadEnvelopeData #################### ");

		PathParam accountFetchAPITypeParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE);

		if (null != accountFetchAPITypeParam) {

			String accountFetchAPIType = accountFetchAPITypeParam.getParamValue();
			if (StringUtils.isEmpty(accountFetchAPIType)
					|| !(AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountFetchAPIType)
							|| AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountFetchAPIType))) {

				throw new InvalidInputException("Wrong accountFetchAPIType " + accountFetchAPIType
						+ " sent, it should be ORGADMIN or USERINFO");
			}

			PathParam inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID);

			if (AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountFetchAPIType) && (null == inputOrgId
					|| (null != inputOrgId && StringUtils.isEmpty(inputOrgId.getParamValue())))) {

				throw new InvalidInputException(
						"InputOrgId is missing or cannot be null for ORGADMIN as accountsFetchAPIToUse");
			}

		} else {

			throw new InvalidInputException("accountFetchAPIType param is missing, it should be ORGADMIN or USERINFO");
		}

		PathParam refreshDataBaseParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.REFRESH_DATA_BASE_FLAG);

		if (null != refreshDataBaseParam) {

			String refreshDataBaseValue = refreshDataBaseParam.getParamValue();

			if (!StringUtils.isEmpty(refreshDataBaseValue) && !("true".equalsIgnoreCase(refreshDataBaseValue)
					|| "false".equalsIgnoreCase(refreshDataBaseValue))) {

				throw new InvalidInputException("Wrong value sent for refreshDataBase -> " + refreshDataBaseValue
						+ ", it should be true or false");
			}
		}

	}

	public List<String> getAllAccountIds(ReportRunArgs apiRunArgs, String apiCategory, Integer apiId) {

		log.info("Fetching AllAccountIds to be processed by this batch");

		List<String> toProcessAccountIdList = null;

		PathParam filterAccountIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.FILTER_ACCOUNT_IDS);

		JWTParams jwtParams = new JWTParams(apiRunArgs);

		if (null != filterAccountIdsParam && !StringUtils.isEmpty(filterAccountIdsParam.getParamValue())) {

			String filterAccountIds = filterAccountIdsParam.getParamValue();

			List<String> filterAccountIdList = Stream.of(filterAccountIds.split(",")).map(String::trim)
					.collect(Collectors.toList());

			String accountsFetchAPIToUse = ReportAppUtil
					.findPathParam(apiRunArgs.getPathParams(), AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE)
					.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				String inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID)
						.getParamValue();

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ORG_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ORGADMINAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);
				toProcessAccountIdList = orgAdminService.getAllAccountIds(inputOrgId, authenticationRequest).stream()
						.filter(accountId -> !filterAccountIdList.contains(accountId)).collect(Collectors.toList());

				return toProcessAccountIdList;
			}

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ACCOUNT_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ESIGNAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

				toProcessAccountIdList = dsAccountService.getAllAccountIds(authenticationRequest).stream()
						.filter(accountId -> !filterAccountIdList.contains(accountId)).collect(Collectors.toList());

				return toProcessAccountIdList;
			}
		} else {

			PathParam selectAccountIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.SELECT_ACCOUNT_IDS);

			if (null != selectAccountIdsParam && !StringUtils.isEmpty(selectAccountIdsParam.getParamValue())) {

				toProcessAccountIdList = Stream.of(selectAccountIdsParam.getParamValue().split(","))
						.collect(Collectors.toList());

				return toProcessAccountIdList;
			}

			String accountsFetchAPIToUse = ReportAppUtil
					.findPathParam(apiRunArgs.getPathParams(), AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE)
					.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				String inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID)
						.getParamValue();

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ORG_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ORGADMINAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);
				toProcessAccountIdList = orgAdminService.getAllAccountIds(inputOrgId, authenticationRequest);

				return toProcessAccountIdList;
			}

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ACCOUNT_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ESIGNAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

				toProcessAccountIdList = dsAccountService.getAllAccountIds(authenticationRequest);

				return toProcessAccountIdList;
			}
		}

		return toProcessAccountIdList;
	}

	public List<String> getAllUserIds(ReportRunArgs apiRunArgs, String accountId, String apiCategory, Integer apiId) {

		log.info("Fetching getAllUserIds to be processed by this batch");

		List<String> toProcessUserIdList = null;

		PathParam filterUserIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.FILTER_USER_IDS);

		JWTParams jwtParams = new JWTParams(apiRunArgs);

		AuthenticationRequest authenticationRequest = new AuthenticationRequest(accountId, jwtParams.getJwtScopes(),
				jwtParams.getJwtUserId(), apiCategory, jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(),
				jwtParams.getAuthUserName(), jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

		if (null != filterUserIdsParam && !StringUtils.isEmpty(filterUserIdsParam.getParamValue())) {

			String filterUserIds = filterUserIdsParam.getParamValue();

			List<String> filterUserIdList = Stream.of(filterUserIds.split(",")).map(String::trim)
					.collect(Collectors.toList());

			toProcessUserIdList = dsAccountService.fetchAllAccountUsers(accountId, authenticationRequest).stream()
					.map(AccountUser::getUserId).filter(userId -> !filterUserIdList.contains(userId))
					.collect(Collectors.toList());

			return toProcessUserIdList;

		} else {

			PathParam selectUserIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.SELECT_USER_IDS);

			if (null != selectUserIdsParam && !StringUtils.isEmpty(selectUserIdsParam.getParamValue())) {

				toProcessUserIdList = Stream.of(selectUserIdsParam.getParamValue().split(","))
						.collect(Collectors.toList());

				return toProcessUserIdList;
			}

			toProcessUserIdList = dsAccountService.fetchAllAccountUsers(accountId, authenticationRequest).stream()
					.map(AccountUser::getUserId).collect(Collectors.toList());

			return toProcessUserIdList;
		}

	}

	public Map<String, Object> prepareInputParams(ReportRunArgs apiRunArgs) {

		Map<String, Object> inputParams = new HashMap<String, Object>();

		List<PathParam> pathParamRemainingList = apiRunArgs.getPathParams();

		pathParamRemainingList.forEach(pathParam -> {

			// For Online mode these values will come in the JSON Body
			// For Batch mode these values will be passed from BatchTriggerInformation
			inputParams.put(pathParam.getParamName(), pathParam.getParamValue());
		});

		PathParam accountsFetchAPIToUsePathParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE);

		if (null != accountsFetchAPIToUsePathParam) {

			String accountsFetchAPIToUse = accountsFetchAPIToUsePathParam.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				inputParams.put(AppConstants.INPUT_ORG_ID, ReportAppUtil
						.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID).getParamValue());
			}
		}

		PathParam refreshDataBaseParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.REFRESH_DATA_BASE_FLAG);

		if (null != refreshDataBaseParam) {

			String refreshDataBaseValue = refreshDataBaseParam.getParamValue();

			if (!StringUtils.isEmpty(refreshDataBaseValue)) {

				inputParams.put(AppConstants.REFRESH_DATA_BASE_FLAG, refreshDataBaseValue);
			}
		}

		log.info("inputParams from reportRunArgs is {}", inputParams);

		return inputParams;

	}

	public void validateDSApiLimit(ApiHourlyLimitData apiHourlyLimitData) {

		// apiThresholdLimitPercent
		log.info(
				"Checking hourly limit apiHourlyLimitData.getRateLimitRemaining() is {} and apiHourlyLimitData.getRateLimitReset() is {}",
				apiHourlyLimitData.getRateLimitRemaining(), apiHourlyLimitData.getRateLimitReset());

		if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getRateLimitRemaining())
				&& !StringUtils.isEmpty(apiHourlyLimitData.getRateLimitReset())) {

			long longEpochTime = Long.parseLong(apiHourlyLimitData.getRateLimitReset());

			Date resetDate = Date.from(Instant.ofEpochSecond(longEpochTime));

			log.debug("Logging resetTime in validateDSApiLimit is {}", resetDate);

			long sleepMillis = DateTimeUtil.getDateDiff(Calendar.getInstance().getTime(), resetDate,
					TimeUnit.MILLISECONDS);

			Float thresholdLimit = Float.parseFloat(apiHourlyLimitData.getRateLimitLimit()) * apiThresholdLimitPercent
					/ 100f;

			log.info(
					"Checking if need to send thread to sleep in PrepareReportDataService or not with sleepMillis -> {}, RateLimitRemaining-> {}, RateLimitResetValue -> {}, Float(RateLimitRemaining) -> {}, Float(RateLimitLimit) -> {} and ThresholdLimit is {}",
					sleepMillis, apiHourlyLimitData.getRateLimitRemaining(), apiHourlyLimitData.getRateLimitReset(),
					Float.parseFloat(apiHourlyLimitData.getRateLimitRemaining()),
					Float.parseFloat(apiHourlyLimitData.getRateLimitLimit()), thresholdLimit);

			if (((Float.parseFloat(apiHourlyLimitData.getRateLimitRemaining()) < thresholdLimit) && sleepMillis > 0)) {

				sleepThread(resetDate, sleepMillis);
			}

		}

		log.info(
				"Checking burst limit apiHourlyLimitData.getBurstLimitRemaining() is {} and apiHourlyLimitData.getBurstLimitLimit() is {}",
				apiHourlyLimitData.getBurstLimitRemaining(), apiHourlyLimitData.getBurstLimitLimit());

		if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getBurstLimitRemaining())) {

			Float thresholdBurstLimit = Float.parseFloat(apiHourlyLimitData.getBurstLimitLimit())
					* apiThresholdLimitPercent / 100f;
			log.info(
					"Checking if need to send thread to sleep in PrepareReportDataService or not with apiHourlyLimitData.getBurstLimitRemaining() is {}, Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()) -> {} and ThresholdBurstLimit is {}",
					apiHourlyLimitData.getBurstLimitRemaining(),
					Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()), thresholdBurstLimit);

			if (Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()) < thresholdBurstLimit) {

				sleepThread(new Date(
						DateTimeUtil.addSecondsAndconvertToEpochTime(DateTimeUtil.currentTimeInString(), 30) * 1000),
						30000);
			}

		}
	}

	public void sleepThread(Date resetDate, long sleepMillis) {

		log.info(
				"Sending thread name-> {} and threadId-> {} to sleep in validateDSApiLimit() for {} milliseconds, and expected to wake up at {}",
				Thread.currentThread().getName(), Thread.currentThread().getId(), sleepMillis + 10000, resetDate);
		try {

			log.info("Sending thread to sleep, resetTime in validateDSApiLimit is {}", resetDate);
			Thread.sleep(sleepMillis + 10000);
		} catch (InterruptedException e) {

			log.error("InterruptedException thrown for thread name- {} and threadId- {}",
					Thread.currentThread().getName(), Thread.currentThread().getId());
			e.printStackTrace();
		}
	}

	public ApiHourlyLimitData readApiHourlyLimitData(HttpHeaders responseHeaders) {

		ApiHourlyLimitData apiHourlyLimitData = null;
		if (null != responseHeaders && null != responseHeaders.entrySet()) {

			apiHourlyLimitData = new ApiHourlyLimitData();

			Set<Entry<String, List<String>>> headerEntrySet = responseHeaders.entrySet();
			for (Entry<String, List<String>> headerEntry : headerEntrySet) {

				log.info("In readApiHourlyLimitData header key is {} and value is {} ", headerEntry.getKey(),
						headerEntry.getValue());

				switch (headerEntry.getKey()) {

				case "X-RateLimit-Reset":
					apiHourlyLimitData.setRateLimitReset(headerEntry.getValue().get(0));
					break;
				case "X-RateLimit-Limit":
					apiHourlyLimitData.setRateLimitLimit(headerEntry.getValue().get(0));
					break;
				case "X-RateLimit-Remaining":
					apiHourlyLimitData.setRateLimitRemaining(headerEntry.getValue().get(0));
					break;
				case "X-BurstLimit-Remaining":
					apiHourlyLimitData.setBurstLimitRemaining(headerEntry.getValue().get(0));
					break;
				case "X-BurstLimit-Limit":
					apiHourlyLimitData.setBurstLimitLimit(headerEntry.getValue().get(0));
					break;
				case "X-DocuSign-TraceToken":
					apiHourlyLimitData.setDocuSignTraceToken(headerEntry.getValue().get(0));
					break;

				default:
					log.warn(
							"-------------------- Wrong case -> {}, this is not handled in this method --------------------",
							headerEntry.getKey());
				}
			}

			validateDSApiLimit(apiHourlyLimitData);
		}

		return apiHourlyLimitData;
	}

}