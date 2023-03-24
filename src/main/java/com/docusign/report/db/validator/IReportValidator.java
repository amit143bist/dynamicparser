package com.docusign.report.db.validator;

import com.docusign.report.domain.ReportInformation;

public interface IReportValidator<T extends ReportInformation> {

	void validateSaveData(T migrationInformation);
}