package com.docusign.report.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "associatedData", "downloadParams" })
public class DownloadDocs {

	@JsonProperty("associatedData")
	private PrepareDataAPI associatedData;
	@JsonProperty("downloadParams")
	private List<PathParam> downloadParams = null;

}