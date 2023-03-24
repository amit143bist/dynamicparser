package com.docusign.report.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.docusign.report.authentication.model.AuthenticationRequest;
import com.docusign.report.authentication.model.JWTParams;
import com.docusign.report.common.constant.APICategoryType;
import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.constant.FailureCode;
import com.docusign.report.common.constant.FailureStep;
import com.docusign.report.common.exception.ResourceNotFoundException;
import com.docusign.report.common.exception.ResourceNotSavedException;
import com.docusign.report.domain.DownloadDocs;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.PrepareDataAPI;
import com.docusign.report.dsapi.domain.EnvelopeDocument;
import com.docusign.report.dsapi.service.DSEnvelopeService;
import com.docusign.report.processor.PrepareAPICallProcessor;
import com.docusign.report.utils.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileWriterService {

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private DSEnvelopeService dsEnvelopeService;

	@Autowired
	private PrepareAPICallProcessor prepareAPICallProcessor;

	public void pullDocumentAndWriteToDirectory(DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			String fileSaveFormat, String fileName, String folderName, Map<String, Object> inputParams,
			String processId) {

		String apiCategory = downloadDocs.getAssociatedData().getApiCategory();

		APICategoryType apiCategoryType = ReportAppUtil.getAPICategoryType(apiCategory);

		if (apiCategoryType == APICategoryType.ESIGNAPI) {

			createIndividualFiles(downloadDocs, batchId, parentDirectory, fileSaveFormat, folderName, inputParams,
					processId);
			createOtherFileTypes(downloadDocs, batchId, parentDirectory, fileSaveFormat, fileName, folderName,
					inputParams, processId);
		} else {

			ResponseEntity<byte[]> respEntity = prepareAPICallProcessor.callPrepareAPIWithRespHeader(
					downloadDocs.getAssociatedData(), AppConstants.NOT_AVAILABLE_CONST, inputParams, null, null,
					batchId, byte[].class, processId);

			if (null != respEntity && null != respEntity.getBody()) {

				if (StringUtils.isEmpty(fileName)) {

					ContentDisposition contentDisposition = respEntity.getHeaders().getContentDisposition();
					fileName = contentDisposition.getFilename();
				}

				MediaType mediaType = respEntity.getHeaders().getContentType();
				if (null != mediaType && !StringUtils.isEmpty(mediaType.getType())
						&& mediaType.getType().toLowerCase().contains("zip")) {

					if (!isDeleteArchiveEnabled(downloadDocs)) {

						writeFileToDirectory(parentDirectory, fileName, folderName, respEntity.getBody(), inputParams,
								downloadDocs.getDownloadParams());
					}

					checkAndUnzipArchive(downloadDocs, fileSaveFormat, parentDirectory, fileName, folderName,
							respEntity.getBody(), inputParams);

				} else {

					writeFileToDirectory(parentDirectory, fileName, folderName, respEntity.getBody(), inputParams,
							downloadDocs.getDownloadParams());
				}
			}
		}
	}

	private void createOtherFileTypes(DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			String fileSaveFormat, String fileName, String folderName, Map<String, Object> inputParams,
			String processId) {

		log.debug("Inside createOtherFileTypes -> {} with fileName as {} and folderName as {}", fileSaveFormat,
				fileName, folderName);
		if ("archive".equalsIgnoreCase(fileSaveFormat) || "combined".equalsIgnoreCase(fileSaveFormat)
				|| "onlycert".equalsIgnoreCase(fileSaveFormat) || "combinedcert".equalsIgnoreCase(fileSaveFormat)) {

			byte[] docBytes = null;
			if ("combinedcert".equalsIgnoreCase(fileSaveFormat)) {

				docBytes = prepareAPICallProcessor.callPrepareAPI(downloadDocs.getAssociatedData(),
						(String) inputParams.get("inputAccountId"), inputParams, batchId, null, byte[].class,
						processId);

				checkFileAndWrite(downloadDocs, batchId, parentDirectory, fileSaveFormat, fileName, folderName,
						inputParams, processId, docBytes);

				docBytes = getESignCert(downloadDocs, batchId, inputParams);

				checkFileAndWrite(downloadDocs, batchId, parentDirectory, fileSaveFormat, "summary.pdf", folderName,
						inputParams, processId, docBytes);

			} else {

				docBytes = prepareAPICallProcessor.callPrepareAPI(downloadDocs.getAssociatedData(),
						(String) inputParams.get("inputAccountId"), inputParams, batchId, null, byte[].class,
						processId);

				checkFileAndWrite(downloadDocs, batchId, parentDirectory, fileSaveFormat, fileName, folderName,
						inputParams, processId, docBytes);

			}

		}
	}

	private byte[] getESignCert(DownloadDocs downloadDocs, String batchId, Map<String, Object> inputParams) {

		byte[] docBytes;
		PrepareDataAPI prepareDataAPI = new PrepareDataAPI();
		prepareDataAPI.setApiId(4);
		prepareDataAPI.setApiCategory(downloadDocs.getAssociatedData().getApiCategory());
		prepareDataAPI.setApiOperationType(downloadDocs.getAssociatedData().getApiOperationType());
		prepareDataAPI.setApiUri("/accounts/{inputAccountId}/envelopes/{inputEnvelopeId}/documents/certificate");
		prepareDataAPI.setApiParams(downloadDocs.getAssociatedData().getApiParams());
		docBytes = prepareAPICallProcessor.callPrepareAPI(prepareDataAPI, (String) inputParams.get("inputAccountId"),
				inputParams, batchId, null, byte[].class, null);
		return docBytes;
	}

	private void checkFileAndWrite(DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			String fileSaveFormat, String fileName, String folderName, Map<String, Object> inputParams,
			String processId, byte[] docBytes) {

		if (null != docBytes) {

			if (!fileName.contains("pdf") && !"archive".equalsIgnoreCase(fileSaveFormat)) {

				fileName = fileName + ".pdf";
			}

			if ("archive".equalsIgnoreCase(fileSaveFormat) && !fileName.contains("zip")) {

				fileName = fileName + ".zip";
			}

			if (!isDeleteArchiveEnabled(downloadDocs)) {

				writeFileToDirectory(parentDirectory, fileName, folderName, docBytes, inputParams,
						downloadDocs.getDownloadParams());
			}
			checkAndUnzipArchive(downloadDocs, fileSaveFormat, parentDirectory, fileName, folderName, docBytes,
					inputParams);
		} else {

			log.warn("No documents returned from DocuSign in createOtherFileTypes for envelopeId {} and accountId {}",
					inputParams.get("envelopeid"), inputParams.get("accountid"));

			Exception exp = new ResourceNotFoundException(
					"No documents returned from DocuSign in createOtherFileTypes");

			batchDataService.createFailureRecord(inputParams.get("accountid") + "_" + inputParams.get("envelopeid"),
					batchId, FailureCode.ERROR_107.toString(), exp.getMessage(),
					FailureStep.PROCESSENVDATAFORWRITINGARCHIVE.toString(), exp);

		}
	}

	private boolean isDeleteArchiveEnabled(DownloadDocs downloadDocs) {

		List<PathParam> downloadParams = downloadDocs.getDownloadParams();
		PathParam deleteArchivePathParam = ReportAppUtil.findPathParam(downloadParams,
				AppConstants.DELETE_ARCHIVE_FLAG);

		return (null != deleteArchivePathParam && !StringUtils.isEmpty(deleteArchivePathParam.getParamValue())
				&& AppConstants.APP_TRUE.equalsIgnoreCase(deleteArchivePathParam.getParamValue()));

	}

	private void createIndividualFiles(DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			String fileSaveFormat, String folderName, Map<String, Object> inputParams, String processId) {

		log.debug("Inside createIndividualFiles with folderName as {}", folderName);
		if ("individual".equalsIgnoreCase(fileSaveFormat) || "individualnocert".equalsIgnoreCase(fileSaveFormat)) {

			JWTParams jwtParams = new JWTParams(downloadDocs.getAssociatedData().getApiRunArgs());

			AuthenticationRequest authenticationRequest = new AuthenticationRequest(
					(String) inputParams.get("inputAccountId"), jwtParams.getJwtScopes(), jwtParams.getJwtUserId(),
					downloadDocs.getAssociatedData().getApiCategory(), jwtParams.getAuthClientId(),
					jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(), jwtParams.getAuthPassword(),
					jwtParams.getAuthBaseUrl(), downloadDocs.getAssociatedData().getApiId());

			List<EnvelopeDocument> envelopeDocuments = dsEnvelopeService.fetchAllDocuments(authenticationRequest,
					(String) inputParams.get("inputEnvelopeId"));

			for (EnvelopeDocument envelopeDocument : envelopeDocuments) {

				if ("individualnocert".equalsIgnoreCase(fileSaveFormat)
						&& "certificate".equalsIgnoreCase(envelopeDocument.getDocumentId())) {
					continue;
				}
				inputParams.put("inputDocumentId", envelopeDocument.getDocumentId());
				byte[] docBytes = prepareAPICallProcessor.callPrepareAPI(downloadDocs.getAssociatedData(),
						(String) inputParams.get("inputAccountId"), inputParams, batchId, null, byte[].class, null);

				if (null != docBytes) {

					String fileName = null;
					if (!envelopeDocument.getName().contains("pdf")) {
						fileName = envelopeDocument.getName() + ".pdf";
					} else {
						fileName = envelopeDocument.getName();
					}

					writeFileToDirectory(parentDirectory, fileName, folderName, docBytes, inputParams,
							downloadDocs.getDownloadParams());
				} else {

					log.warn(
							"No documents returned from DocuSign in createIndividualFiles for envelopeId {} and accountId {}",
							inputParams.get("envelopeid"), inputParams.get("accountid"));

					Exception exp = new ResourceNotFoundException(
							"No documents returned from DocuSign in createIndividualFiles");

					batchDataService.createFailureRecord(
							inputParams.get("accountid") + "_" + inputParams.get("envelopeid"), batchId,
							FailureCode.ERROR_107.toString(), exp.getMessage(),
							FailureStep.PROCESSENVDATAFORWRITINGNONARCHIVE.toString(), exp);

				}
			}
		}
	}

	private void writeFileToDirectory(Path parentDirectory, String fileName, String folderName, byte[] docBytes,
			Map<String, Object> inputParams, List<PathParam> downloadParams) {

		PathParam restrictedRegexExpressionParam = ReportAppUtil.findPathParam(downloadParams,
				AppConstants.DOWNLOAD_FOLDER_FILE_RESTRICTED_REGEX_PARAM_NAME);

		if (null != restrictedRegexExpressionParam
				&& !StringUtils.isEmpty(restrictedRegexExpressionParam.getParamValue())) {

			fileName = fileName.replaceAll(restrictedRegexExpressionParam.getParamValue(),
					AppConstants.RESTRICTED_CHARACTER_REPLACEMENT);// Removing
			// whitespaces
			// from the
			// foldername
		}

		PathParam disableAccountIdFolderPathParam = ReportAppUtil.findPathParam(downloadParams,
				AppConstants.DISABLE_ACCOUNTID_IN_DOC_FOLDER_PATH_FLAG);

		boolean includeAccountIdInFolderPath = true;
		if (null != disableAccountIdFolderPathParam
				&& !StringUtils.isEmpty(disableAccountIdFolderPathParam.getParamValue())) {

			String disableAccountIdFolderPathValue = disableAccountIdFolderPathParam.getParamValue();

			if (AppConstants.APP_TRUE.equalsIgnoreCase(disableAccountIdFolderPathValue)) {

				includeAccountIdInFolderPath = false;
			}
		}

		String accountIdAsFolderName = (String) inputParams.get("inputAccountId");
		if (StringUtils.isEmpty(folderName)) {

			String newDirectoryPath = null;
			if (includeAccountIdInFolderPath) {

				newDirectoryPath = parentDirectory + File.separator + accountIdAsFolderName;
			} else {

				newDirectoryPath = parentDirectory.toString();
			}

			ReportAppUtil.createDirectoryNIO(newDirectoryPath);

			ReportAppUtil.writeBytesToFileNio(docBytes, newDirectoryPath + File.separator + fileName);
		} else {

			if (null != restrictedRegexExpressionParam
					&& !StringUtils.isEmpty(restrictedRegexExpressionParam.getParamValue())) {

				folderName = folderName.replaceAll(restrictedRegexExpressionParam.getParamValue(),
						AppConstants.RESTRICTED_CHARACTER_REPLACEMENT);// Removing
				// whitespaces
				// from
				// the
				// foldername
			}

			String newDirectoryPath = null;
			if (includeAccountIdInFolderPath) {

				newDirectoryPath = parentDirectory + File.separator + accountIdAsFolderName + File.separator
						+ folderName;

			} else {

				newDirectoryPath = parentDirectory + File.separator + folderName;
			}

			ReportAppUtil.createDirectoryNIO(newDirectoryPath);

			ReportAppUtil.writeBytesToFileNio(docBytes, newDirectoryPath + File.separator + fileName);
		}
	}

	private void checkAndUnzipArchive(DownloadDocs downloadDocs, String fileSaveFormat, Path parentDirectory,
			String fileName, String folderName, byte[] docBytes, Map<String, Object> inputParams) {

		if ("archive".equalsIgnoreCase(fileSaveFormat)) {

			List<PathParam> downloadParams = downloadDocs.getDownloadParams();

			PathParam unzipPathParam = ReportAppUtil.findPathParam(downloadParams, AppConstants.UNZIP_ARCHIVE_FLAG);

			PathParam disableAccountIdFolderPathParam = ReportAppUtil.findPathParam(downloadParams,
					AppConstants.DISABLE_ACCOUNTID_IN_DOC_FOLDER_PATH_FLAG);

			PathParam deleteArchivePathParam = ReportAppUtil.findPathParam(downloadParams,
					AppConstants.DELETE_ARCHIVE_FLAG);

			if (null != unzipPathParam && !StringUtils.isEmpty(unzipPathParam.getParamValue())) {

				String unZipArchiveFlag = unzipPathParam.getParamValue();

				if (AppConstants.APP_TRUE.equalsIgnoreCase(unZipArchiveFlag)) {

					String newDirectoryPath = null;
					String srcZip = null;

					PathParam restrictedRegexExpressionParam = ReportAppUtil.findPathParam(downloadParams,
							AppConstants.DOWNLOAD_FOLDER_FILE_RESTRICTED_REGEX_PARAM_NAME);

					if (null != restrictedRegexExpressionParam
							&& !StringUtils.isEmpty(restrictedRegexExpressionParam.getParamValue())) {

						fileName = fileName.replaceAll(restrictedRegexExpressionParam.getParamValue(),
								AppConstants.RESTRICTED_CHARACTER_REPLACEMENT);// Removing
						// whitespaces
						// from the
						// foldername
					}

					boolean includeAccountIdInFolderPath = true;
					if (null != disableAccountIdFolderPathParam
							&& !StringUtils.isEmpty(disableAccountIdFolderPathParam.getParamValue())) {

						String disableAccountIdFolderPathValue = disableAccountIdFolderPathParam.getParamValue();

						if (AppConstants.APP_TRUE.equalsIgnoreCase(disableAccountIdFolderPathValue)) {

							includeAccountIdInFolderPath = false;
						}
					}

					String accountIdAsFolderName = (String) inputParams.get("inputAccountId");
					if (StringUtils.isEmpty(folderName)) {

						if (includeAccountIdInFolderPath) {

							newDirectoryPath = parentDirectory + File.separator + accountIdAsFolderName;
						} else {

							newDirectoryPath = parentDirectory.toString();
						}

						ReportAppUtil.createDirectoryNIO(newDirectoryPath);
						srcZip = newDirectoryPath + File.separator + fileName;

					} else {

						if (null != restrictedRegexExpressionParam
								&& !StringUtils.isEmpty(restrictedRegexExpressionParam.getParamValue())) {

							folderName = folderName.replaceAll(restrictedRegexExpressionParam.getParamValue(),
									AppConstants.RESTRICTED_CHARACTER_REPLACEMENT); // Removing
																					// whitespaces
																					// from
																					// the
																					// foldername
						}

						if (includeAccountIdInFolderPath) {

							newDirectoryPath = parentDirectory + File.separator + accountIdAsFolderName + File.separator
									+ folderName;
						} else {

							newDirectoryPath = parentDirectory + File.separator + folderName;
						}

						ReportAppUtil.createDirectoryNIO(newDirectoryPath);
						srcZip = newDirectoryPath + File.separator + fileName;

					}

					unZipAndDeleteArchive(srcZip, new File(newDirectoryPath), deleteArchivePathParam, docBytes);

					log.info("All files written to {} for fileName -> {}", newDirectoryPath, fileName);
				}

			}
		}
	}

	private void unZipAndDeleteArchive(String srcZip, File destUnZipDir, PathParam deleteArchivePathParam,
			byte[] docBytes) {

		byte[] buffer = new byte[1024];
		ZipInputStream zis;
		try {

			if (null != docBytes) {

				zis = new ZipInputStream(new ByteArrayInputStream(docBytes));
			} else {

				zis = new ZipInputStream(new FileInputStream(srcZip));
			}

			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {

				File newFile = newFile(destUnZipDir, zipEntry);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();

			if (null == docBytes) {

				deleteArchive(srcZip, deleteArchivePathParam);
			}

		} catch (FileNotFoundException e) {

			log.error("FileNotFoundException {} occurred with the message {} and cause {}", e, e.getMessage(), e);
			e.printStackTrace();
			throw new ResourceNotFoundException("File " + srcZip + " not found in ");
		} catch (IOException e) {

			log.error("IOException {} occurred with the message {} and cause {}", e, e.getMessage(), e);
			e.printStackTrace();
			throw new ResourceNotSavedException("File not saved in " + destUnZipDir);
		} catch (Exception e) {

			log.error("Exception {} occurred with the message {} and cause {}", e, e.getMessage(), e);
			e.printStackTrace();
			throw new ResourceNotSavedException("File not saved in " + destUnZipDir);
		}

	}

	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {

		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {

			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	private void deleteArchive(String srcZip, PathParam deleteArchivePathParam) {

		if (null != deleteArchivePathParam && !StringUtils.isEmpty(deleteArchivePathParam.getParamValue())) {

			String deleteArchivePathValue = deleteArchivePathParam.getParamValue();

			if (AppConstants.APP_TRUE.equalsIgnoreCase(deleteArchivePathValue)) {

				if(log.isDebugEnabled()) {
				
					log.debug("Deleting Zip file -> {}", srcZip);
				}
				
				File file = new File(srcZip);
				file.delete();
			}
		}
	}
}