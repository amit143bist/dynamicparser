package com.docusign.report.common.constant;

public interface AppConstants {

	String NAF = "naf";

	String COLON = ":";

	String APP_TRUE = "true";

	String FORWARD_SLASH = "/";

	String JWT_SCOPES = "scopes";

	String JWT_USER_ID = "userId";

	String BATCH_TYPE = "batchType";

	String ROOMS_ADMIN = "roomsAdmin";

	String AUTH_CLIENTID = "clientId";

	String INPUT_ORG_ID = "inputOrgId";

	String INPUT_TO_DATE = "inputToDate";

	String SCRIPT_ENGINE_NAME = "nashorn";

	String ACCOUNT_ADMIN = "accountAdmin";

	String ORG_ADMIN = "organizationAdmin";

	String INPUT_FROM_DATE = "inputFromDate";

	String SELECT_USER_IDS = "selectUserIds";

	String FILTER_USER_IDS = "filterUserIds";

	String AUTH_CLIENTSECRET = "clientSecret";

	String FILE_SAVE_FORMAT = "fileSaveFormat";

	String UNZIP_ARCHIVE_FLAG = "unzipArchive";

	String DELETE_ARCHIVE_FLAG = "deleteArchive";

	String REFRESH_DATA_BASE = "refreshDataBase";

	String RESTRICTED_CHARACTER_REPLACEMENT = "_";

	String SECRETKEY_TYPE = "PBKDF2WithHmacSHA256";

	String SELECT_ACCOUNT_IDS = "selectAccountIds";

	String FILTER_ACCOUNT_IDS = "filterAccountIds";

	String DOWNLOAD_FILE_NAME = "downloadFileName";

	String PROCESS_ALL_USERS_FLAG = "processAllUsers";

	String REFRESH_DATA_BASE_FLAG = "refreshDataBase";

	String DOWNLOAD_FOLDER_NAME = "downloadFolderName";

	String CIPHER_INSTANCE_TYPE = "AES/CBC/PKCS5Padding";

	String RESTRICTED_REGEX_EXPRESSION = "[\\\\/:*?\"<>|]";

	String COMPLETE_BATCH_ON_ERROR = "completeBatchOnError";

	String CSV_DOWNLOAD_FOLDER_PATH = "csvDownloadFolderPath";// CSV Download Directory path

	String DOC_DOWNLOAD_FOLDER_PATH = "docDownloadFolderPath";// Doc download path

	String DOC_DOWNLOAD_DESTINATION = "docDownloadDestination";// Sharepoint, Disk, S3 et al

	String ACCOUNTS_FETCH_API_TO_USE_TYPE = "accountsFetchAPIToUse";

	String DISABLE_CURR_DATE_IN_CSV_FOLDER_PATH_FLAG = "disableCurrentDateInCSVFolderPath";// If set as True then no
																							// directory will be created
																							// with currdate

	String DISABLE_CURR_DATE_IN_DOC_FOLDER_PATH_FLAG = "disableCurrentDateInDocFolderPath";// If set as True then no
																							// directory will be created
																							// with currdate

	String COMMA_DELIMITER = ",";
	
	String CSV_DOWNLOAD_ROWS_LIMT_PER_FILE = "csvDownloadRowsLimit";// CSV Download rows limit per file
	
	String ACCOUNT_ID_COL_NAME = "accountIdColumnName";

	String ENVELOPE_ID_COL_NAME = "envelopeIdColumnName";
	
	String DOC_DOWNLOAD_COLUMN_LABELS = "docDownloadColumnLabels";
	
	String CSV_FETCH_COUNT_COLUMN_NAME = "csvCountColumnName";// CSV Column name to count the limit
	
	String NOT_AVAILABLE_CONST = "NOTAVAILABLE";
	
	String AUTH_API_USERNAME = "apiUserName";

	String AUTH_API_PASSWORD = "apiPassword";

	String AUTH_API_BASEURL = "apiBaseUrl";
	
	String DS_ACCOUNT_ID = "dsAccountId";
	
	String DOWNLOAD_FOLDER_FILE_RESTRICTED_REGEX_PARAM_NAME = "downloadFolderFileRestrictedRegex";
	
	String DISABLE_ACCOUNTID_IN_DOC_FOLDER_PATH_FLAG = "disableAccountIdInDocFolderPath";// If set as True then no
	// directory will be created
	// with currdate
	
	String REPORT_DATA_PAGINATION_LIMIT = "reportDataPaginationLimit";// CSV Download rows limit per file
	
	String SQL_QUERY_OFFSET = "offset";
	String SQL_QUERY_LIMIT = "limit";
}