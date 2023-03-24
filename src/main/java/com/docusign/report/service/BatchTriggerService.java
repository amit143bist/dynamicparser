package com.docusign.report.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.docusign.report.cipher.AESCipher;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.JobType;
import com.docusign.report.common.exception.BatchNotAuthorizedException;
import com.docusign.report.common.exception.JSONConversionException;
import com.docusign.report.common.exception.ResourceConditionFailedException;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.RunningBatchException;
import com.docusign.report.controller.CoreManageReportDataController;
import com.docusign.report.controller.CorePrepareReportDataController;
import com.docusign.report.db.controller.CoreCacheDataLogController;
import com.docusign.report.db.controller.CoreScheduledBatchLogController;
import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.db.domain.ScheduledBatchLogResponse;
import com.docusign.report.domain.BatchStartParams;
import com.docusign.report.domain.BatchTriggerInformation;
import com.docusign.report.domain.ManageDataAPI;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.domain.PrepareReportDefinition;
import com.docusign.report.utils.DateTimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchTriggerService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CoreManageReportDataController coreManageReportDataController;

	@Autowired
	private CoreScheduledBatchLogController coreScheduledBatchLogController;

	@Autowired
	private CorePrepareReportDataController corePrepareReportDataController;

	@Autowired
	private CoreCacheDataLogController coreCacheDataLogController;

	@Value("${app.ruleEngineJsonFilePath}")
	private String ruleEngineJsonFilePath;

	@Value("${app.authorization.integratorKey}")
	private String integratorKey;

	@Value("${app.db.auditorname}")
	private String auditorName;

	public void callService(BatchTriggerInformation batchTriggerInformation) throws Exception {

//		checkIfAuthorizedToRunApp();

		PrepareReportDefinition prepareReportDefinition = null;

		prepareReportDefinition = objectMapper.readValue(new FileReader(new File(ruleEngineJsonFilePath)),
				PrepareReportDefinition.class);

		log.info("prepareReportDefinition loaded and created for jobType -> {}", batchTriggerInformation.getJobType());

		if (JobType.MANAGEDATA.toString().equalsIgnoreCase(batchTriggerInformation.getJobType())) {

			for (ManageDataAPI manageDataAPI : prepareReportDefinition.getManageDataAPIs()) {

				setupInputParams(batchTriggerInformation, prepareReportDefinition,
						manageDataAPI.getExportRunArgs().getPathParams(),
						manageDataAPI.getExportRunArgs().getBatchType());
			}
		} else {

			for (PrepareDataAPI prepareDataAPI : prepareReportDefinition.getPrepareDataAPIs()) {

				setupInputParams(batchTriggerInformation, prepareReportDefinition,
						prepareDataAPI.getApiRunArgs().getPathParams(), prepareDataAPI.getApiRunArgs().getBatchType());
			}

		}

		loadBatchWithValidParams(batchTriggerInformation, prepareReportDefinition);
	}

	void checkIfAuthorizedToRunApp() {

		try {

			CacheLogInformation cacheLogInformation = coreCacheDataLogController
					.findByCacheKeyAndCacheReference(AppConstants.NAF, AppConstants.NAF).getBody();

			String encryptedNAF = cacheLogInformation.getCacheValue();

			final String secretKey = new String(
					Base64.getEncoder().encode((auditorName + AppConstants.COLON + integratorKey).getBytes()));
			final String salt = new String(
					Base64.getEncoder().encode((integratorKey + AppConstants.COLON + auditorName).getBytes()));

			Long naf = Long.parseLong(AESCipher.decrypt(encryptedNAF, secretKey, salt));

			Long currentTime = DateTimeUtil.currentEpochTime();

			if (currentTime > naf) {

				throw new BatchNotAuthorizedException(
						" $$$$$$$$$$$$$$$$$$$$ Not Authorized to trigger this job $$$$$$$$$$$$$$$$$$$$ ");
			}

		} catch (ResourceNotFoundException exp) {

			throw new ResourceConditionFailedException(
					" !!!!!!!!!!!!!!!!!!!! Required inputs not ready to trigger the batch  !!!!!!!!!!!!!!!!!!!! ");

		}
	}

	private void setupInputParams(BatchTriggerInformation batchTriggerInformation,
			PrepareReportDefinition prepareReportDefinition, List<PathParam> pathParamList, String batchType) {

		try {

			ResponseEntity<ScheduledBatchLogResponse> scheduledBatchLogResponseEntity = coreScheduledBatchLogController
					.findLatestBatchByBatchType(batchType);

			ScheduledBatchLogResponse scheduledBatchLogResponse = scheduledBatchLogResponseEntity.getBody();

			if (isCompleteBatchOnError(prepareReportDefinition)
					|| null != scheduledBatchLogResponse.getBatchEndDateTime()) {

				if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

					log.info(
							"Successfully found last completed batch job of jobType -> {}, last completed batchId is {}",
							batchTriggerInformation.getJobType(), scheduledBatchLogResponse.getBatchId());
				} else {
					log.error(
							" ------------------------------ Another Batch running of jobType -> {} since {} ------------------------------ ",
							batchTriggerInformation.getJobType(),
							DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
									scheduledBatchLogResponse.getBatchStartDateTime(), null));
				}

				calculateBatchTriggerParameters(scheduledBatchLogResponse, batchTriggerInformation, pathParamList);

			} else {

				log.error(
						" ------------------------------ Another Batch running of jobType -> {} since {} ------------------------------ ",
						batchTriggerInformation.getJobType(), DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
								scheduledBatchLogResponse.getBatchStartDateTime(), null));

				throw new RunningBatchException(
						"Another Batch already running for batch type " + batchTriggerInformation.getJobType()
								+ " since " + DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(
										scheduledBatchLogResponse.getBatchStartDateTime(), null));
			}

		} catch (ResourceNotFoundException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("In ResourceNotFoundException block, No Batch running of jobType -> {}",
					batchTriggerInformation.getJobType());

			calculateBatchTriggerParameters(null, batchTriggerInformation, pathParamList);

		}
	}

	private boolean isCompleteBatchOnError(PrepareReportDefinition prepareReportDefinition) {

		return prepareReportDefinition.getJobRunArgs().isCompleteBatchOnError();
	}

	private void loadBatchWithValidParams(BatchTriggerInformation batchTriggerInformation,
			PrepareReportDefinition prepareReportDefinition) throws Exception {

		if (JobType.MANAGEDATA.toString().equalsIgnoreCase(batchTriggerInformation.getJobType())) {

			coreManageReportDataController.manageReportData(prepareReportDefinition, null, null);
		} else {

			corePrepareReportDataController.prepareReportData(prepareReportDefinition, null);
		}
	}

	public void calculateBatchTriggerParameters(ScheduledBatchLogResponse scheduledBatchLogResponse,
			BatchTriggerInformation batchTriggerInformation, List<PathParam> pathParamList) {

		String newBatchStartDateTime = null;
		String newBatchEndDateTime = null;

		if (null != batchTriggerInformation.getBatchStartDateTime()) {

			// BatchStartDateTime is sent with other params
			if (null != batchTriggerInformation.getBatchEndDateTime()) {

				// Both BatchStartDateTime and BatchEndDateTime are sent in the request
				newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();

				newBatchEndDateTime = batchTriggerInformation.getBatchEndDateTime();
			} else {

				if (null != batchTriggerInformation.getNumberOfHours()
						&& batchTriggerInformation.getNumberOfHours() > -1) {

					// BatchStartDateTime and NumberOfHours are sent in the request
					newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();

					newBatchEndDateTime = DateTimeUtil.convertToStringFromEpochTimeInSecs(
							DateTimeUtil.addHoursAndconvertToEpochTime(batchTriggerInformation.getBatchStartDateTime(),
									batchTriggerInformation.getNumberOfHours()));

				} else {

					// Only BatchStartDateTime is sent in the request
					newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();

					newBatchEndDateTime = DateTimeUtil
							.convertToStringFromEpochTimeInSecs(DateTimeUtil.currentEpochTime());

				}
			}
		} else if (null != batchTriggerInformation.getNumberOfHours()
				&& batchTriggerInformation.getNumberOfHours() > -1) {

			// Only NumberOfHours are sent in the request
			String lastBatchParameters = scheduledBatchLogResponse.getBatchStartParameters();

			BatchStartParams startParams;
			try {

				startParams = objectMapper.readValue(lastBatchParameters, BatchStartParams.class);
			} catch (IOException e) {

				log.error(
						"JSON Mapping error occured in converting to BatchStartParams for string {} in calculateBatchTriggerParameters",
						lastBatchParameters);
				throw new JSONConversionException(
						"JSON Mapping error occured in converting to BatchStartParams in calculateBatchTriggerParameters",
						e.getCause());
			}

			newBatchStartDateTime = DateTimeUtil.convertToStringFromEpochTimeInSecs(startParams.getEndDateTime() + 1);

			Long calculatedTime = startParams.getEndDateTime() + 3600 * batchTriggerInformation.getNumberOfHours();
			Long currentTime = DateTimeUtil.currentEpochTime();

			if (calculatedTime > currentTime) {// BatchEndDateTime should not be greater than currentdatetime

				newBatchEndDateTime = DateTimeUtil.convertToStringFromEpochTimeInSecs(currentTime);
			} else {

				newBatchEndDateTime = DateTimeUtil.convertToStringFromEpochTimeInSecs(calculatedTime);
			}

		}

		log.info("Inside calculateBatchTriggerParameters, newBatchStartDateTime is {} and newBatchEndDateTime is {}",
				newBatchStartDateTime, newBatchEndDateTime);

		populatePathParamList(pathParamList, newBatchStartDateTime, newBatchEndDateTime, batchTriggerInformation);

	}

	private void populatePathParamList(List<PathParam> pathParamList, String newBatchStartDateTime,
			String newBatchEndDateTime, BatchTriggerInformation batchTriggerInformation) {

		PathParam pathParam = new PathParam();
		pathParam.setParamName(AppConstants.INPUT_FROM_DATE);
		pathParam.setParamValue(newBatchStartDateTime);

		pathParamList.add(pathParam);

		pathParam = new PathParam();
		pathParam.setParamName(AppConstants.INPUT_TO_DATE);
		pathParam.setParamValue(newBatchEndDateTime);

		pathParamList.add(pathParam);

		log.info("Job's pathParamList size before is {}", pathParamList.size());

		List<PathParam> dynamicBatchTiggerPathParams = batchTriggerInformation.getPathParams();

		if (null != dynamicBatchTiggerPathParams && !dynamicBatchTiggerPathParams.isEmpty()) {

			log.info("Overriding or adding new PathParam from batchTriggerInformation to the job");

			// Replace (aka override) path from batchTriggerInformation to the job's
			// pathParamList if param name is same
			Iterator<PathParam> existingPathParamIterator = pathParamList.iterator();

			while (existingPathParamIterator.hasNext()) {

				PathParam existingPathParam = existingPathParamIterator.next();
				PathParam dynamicPathParam = dynamicBatchTiggerPathParams.stream()
						.filter(batchTriggerParam -> batchTriggerParam.getParamName()
								.equalsIgnoreCase(existingPathParam.getParamName()))
						.findAny().orElse(null);

				if (null != dynamicPathParam) {

					existingPathParam.setParamValue(dynamicPathParam.getParamValue());
				}
			}

			// Add new Path from batchTriggerInformation to the job's pathParamList
			Iterator<PathParam> dynamicPathParamIterator = dynamicBatchTiggerPathParams.iterator();

			while (dynamicPathParamIterator.hasNext()) {

				PathParam dynamicPathParam = dynamicPathParamIterator.next();

				PathParam filteredExistingPathParam = pathParamList.stream()
						.filter(existingPathParam -> existingPathParam.getParamName()
								.equalsIgnoreCase(dynamicPathParam.getParamName()))
						.findAny().orElse(null);

				if (null == filteredExistingPathParam) {

					pathParamList.add(dynamicPathParam);
				}
			}

		}

		log.info("Job's pathParamList size after is {}", pathParamList.size());
	}
}