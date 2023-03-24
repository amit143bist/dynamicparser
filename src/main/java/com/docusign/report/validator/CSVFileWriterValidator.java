package com.docusign.report.validator;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.docusign.report.common.constant.FileSaveFormat;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.domain.DownloadDocs;
import com.docusign.report.domain.PathParam;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CSVFileWriterValidator {

	public void validateDownloadDocs(DownloadDocs downloadDocs, List<PathParam> downloadParams) {

		if (null == downloadDocs.getAssociatedData() || null == downloadDocs.getAssociatedData().getApiUri()
				|| null == downloadParams) {

			throw new InvalidInputException(
					"Either AssociatedData or apiUri or downloadParams is/are null in downloadDocs property");
		}

		if (null == downloadDocs.getAssociatedData().getApiParams()
				|| downloadDocs.getAssociatedData().getApiParams().isEmpty()) {

			throw new InvalidInputException("ApiParams cannot be null or empty");
		}
	}

	public void validateFileSaveFormatWithAPIUri(String fileSaveFormat, String apiUri) {

		if (FileSaveFormat.INDIVIDUAL.toString().equalsIgnoreCase(fileSaveFormat)
				&& !apiUri.contains("{inputDocumentId}")) {

			throw new InvalidInputException(
					"{inputDocumentId} is not present in apiuri when fileSaveformat is INDIVIDUAL");
		}

		if (FileSaveFormat.INDIVIDUALNOCERT.toString().equalsIgnoreCase(fileSaveFormat)
				&& !apiUri.contains("{inputDocumentId}")) {

			throw new InvalidInputException(
					"{inputDocumentId} is not present in apiuri when fileSaveformat is INDIVIDUALNOCERT");
		}

		if (FileSaveFormat.ARCHIVE.toString().equalsIgnoreCase(fileSaveFormat) && !apiUri.contains("archive")) {

			throw new InvalidInputException("archive is not present in apiuri when fileSaveformat is ARCHIVE");
		}

		if (FileSaveFormat.COMBINED.toString().equalsIgnoreCase(fileSaveFormat) && !apiUri.contains("combined")) {

			throw new InvalidInputException("archive is not present in apiuri when fileSaveformat is COMBINED");
		}

		if (FileSaveFormat.ONLYCERT.toString().equalsIgnoreCase(fileSaveFormat) && !apiUri.contains("certificate")) {

			throw new InvalidInputException("certificate is not present in apiuri when fileSaveformat is ONLYCERT");
		}

	}

	public void validateFileSaveFormatEnum(String fileSaveFormat) {

		if (StringUtils.isEmpty(fileSaveFormat)) {

			log.error("fileSaveFormat cannot be null, it should archive/combined/individual/onlycert/individualnocert",
					fileSaveFormat);
			throw new InvalidInputException("fileSaveFormat cannot be null");
		}

		if (!EnumUtils.isValidEnum(FileSaveFormat.class, fileSaveFormat.toUpperCase())) {

			log.error("{} is incorrect fileSaveFormat, it should archive/combined/individual/onlycert/individualnocert",
					fileSaveFormat);
			throw new InvalidInputException(fileSaveFormat + " is incorrect fileSaveFormat");
		}
	}
}