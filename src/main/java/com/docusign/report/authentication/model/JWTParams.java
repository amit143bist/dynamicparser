package com.docusign.report.authentication.model;

import com.docusign.report.common.constant.AppConstants;
import com.docusign.report.common.exception.InvalidInputException;
import com.docusign.report.domain.PathParam;
import com.docusign.report.domain.ReportRunArgs;
import com.docusign.report.utils.ReportAppUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JWTParams {

	private String jwtScopes;
	private String jwtUserId;
	private String authClientId;
	private String authClientSecret;
	private String authUserName;
	private String authPassword;
	private String authBaseUrl;

	public JWTParams(ReportRunArgs apiRunArgs) {

		log.info("Setting JWT params");

		if (log.isDebugEnabled()) {

			log.debug("Setting JWT params for apiRunArgs -> {}", apiRunArgs);
		}

		if (null != apiRunArgs && null != apiRunArgs.getPathParams()) {

			PathParam scopesParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.JWT_SCOPES);
			PathParam userIdParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.JWT_USER_ID);

			PathParam clientIdParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.AUTH_CLIENTID);
			PathParam clientSecretParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.AUTH_CLIENTSECRET);

			PathParam authUserNameParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.AUTH_API_USERNAME);
			PathParam authPasswordParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.AUTH_API_PASSWORD);

			PathParam authBaseUrlParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.AUTH_API_BASEURL);

			validateAuthParams(clientIdParam, clientSecretParam, AppConstants.JWT_USER_ID, AppConstants.JWT_SCOPES);
			validateAuthParams(clientIdParam, clientSecretParam, AppConstants.AUTH_CLIENTID,
					AppConstants.AUTH_CLIENTSECRET);
			validateAuthParams(authUserNameParam, authPasswordParam, AppConstants.AUTH_API_USERNAME,
					AppConstants.AUTH_API_PASSWORD);

			if (null != scopesParam) {

				this.jwtScopes = scopesParam.getParamValue();
			}

			if (null != userIdParam) {

				this.jwtUserId = userIdParam.getParamValue();
			}

			if (null != clientIdParam) {

				this.authClientId = clientIdParam.getParamValue();
			}

			if (null != clientSecretParam) {

				this.authClientSecret = clientSecretParam.getParamValue();
			}

			if (null != authUserNameParam) {

				this.authUserName = authUserNameParam.getParamValue();
			}

			if (null != authPasswordParam) {

				this.authPassword = authPasswordParam.getParamValue();
			}

			if (null != authBaseUrlParam) {

				this.authBaseUrl = authBaseUrlParam.getParamValue();
			}
		}

	}

	private void validateAuthParams(PathParam clientIdParam, PathParam clientSecretParam, String paramNameA,
			String paramNameB) {

		if (null != clientIdParam && null == clientSecretParam) {

			throw new InvalidInputException(paramNameA + " cannot be null in apiRunArgs");
		}

		if (null == clientIdParam && null != clientSecretParam) {

			throw new InvalidInputException(paramNameB + " cannot be null in apiRunArgs");
		}
	}
}