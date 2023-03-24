package com.docusign.report.dsapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "cultureName", "nameFormat", "initialFormat", "addressFormat", "dateFormat", "timeFormat",
		"calendarType", "timeZone", "currencyPositiveFormat", "currencyNegativeFormat", "effectiveNameFormat",
		"effectiveInitialFormat", "effectiveAddressFormat", "effectiveDateFormat", "effectiveTimeFormat",
		"effectiveCalendarType", "effectiveTimeZone", "effectiveCurrencyPositiveFormat",
		"effectiveCurrencyNegativeFormat", "currencyCode", "effectiveCurrencyCode", "signDateFormat",
		"signTimeFormat" })
public class LocalePolicy {

	@JsonProperty("cultureName")
	private String cultureName;
	@JsonProperty("nameFormat")
	private String nameFormat;
	@JsonProperty("initialFormat")
	private String initialFormat;
	@JsonProperty("addressFormat")
	private String addressFormat;
	@JsonProperty("dateFormat")
	private String dateFormat;
	@JsonProperty("timeFormat")
	private String timeFormat;
	@JsonProperty("calendarType")
	private String calendarType;
	@JsonProperty("timeZone")
	private String timeZone;
	@JsonProperty("currencyPositiveFormat")
	private String currencyPositiveFormat;
	@JsonProperty("currencyNegativeFormat")
	private String currencyNegativeFormat;
	@JsonProperty("effectiveNameFormat")
	private String effectiveNameFormat;
	@JsonProperty("effectiveInitialFormat")
	private String effectiveInitialFormat;
	@JsonProperty("effectiveAddressFormat")
	private String effectiveAddressFormat;
	@JsonProperty("effectiveDateFormat")
	private String effectiveDateFormat;
	@JsonProperty("effectiveTimeFormat")
	private String effectiveTimeFormat;
	@JsonProperty("effectiveCalendarType")
	private String effectiveCalendarType;
	@JsonProperty("effectiveTimeZone")
	private String effectiveTimeZone;
	@JsonProperty("effectiveCurrencyPositiveFormat")
	private String effectiveCurrencyPositiveFormat;
	@JsonProperty("effectiveCurrencyNegativeFormat")
	private String effectiveCurrencyNegativeFormat;
	@JsonProperty("currencyCode")
	private String currencyCode;
	@JsonProperty("effectiveCurrencyCode")
	private String effectiveCurrencyCode;
	@JsonProperty("signDateFormat")
	private String signDateFormat;
	@JsonProperty("signTimeFormat")
	private String signTimeFormat;

}