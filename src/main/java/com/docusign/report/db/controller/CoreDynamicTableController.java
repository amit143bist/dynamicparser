package com.docusign.report.db.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.db.transformer.DynamicTableTransformer;
import com.docusign.report.domain.TableColumnMetaData;
import com.docusign.report.domain.TableCreationRequest;
import com.docusign.report.domain.TableDefinition;
import com.docusign.report.service.ReportJDBCService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreDynamicTableController {

	@Autowired
	ReportJDBCService reportJDBCService;

	@Autowired
	DynamicTableTransformer dynamicTableTransformer;

	@Autowired
	CoreCacheDataLogController coreCacheDataLogController;

	@PostMapping("/report/createtables")
	public void createDynamicTables(@RequestBody TableCreationRequest tableCreationRequest) {

		log.info("<<<<<<<<<<<<<<<<<<<< Creating Dynamic Tables >>>>>>>>>>>>>>>>>>>>");

		List<TableDefinition> tableDefinitions = tableCreationRequest.getTableDefinitions();

		List<String> ddlQueries = new ArrayList<String>();
		List<CacheLogInformation> cacheLogInformationList = new ArrayList<CacheLogInformation>();
		tableDefinitions.forEach(tableDefinition -> {

			log.info("Creating table -> {}", tableDefinition.getTableName());
			ddlQueries.add(dynamicTableTransformer.createTableQuery(tableDefinition));

			cacheLogInformationList.addAll(dynamicTableTransformer.createCacheLogInfoList(tableDefinition));
		});

		ddlQueries.forEach(ddl -> {

			log.debug(" :::::: ddlQuery :::::: " + ddl);

		});

		log.info(" <<<<<<<<<<<<<<<<<<<<< Calling JDBC to create table in the database >>>>>>>>>>>>>>>>>> ");
		String[] ddlArr = new String[ddlQueries.size()];
		reportJDBCService.createTables(ddlQueries.toArray(ddlArr));

		if (null != cacheLogInformationList && !cacheLogInformationList.isEmpty()) {

			log.info(
					" <<<<<<<<<<<<<<<<<<<<< Creating ColumnName and CSVHeader Mapping in the database >>>>>>>>>>>>>>>>>>");
			coreCacheDataLogController.saveAllCache(cacheLogInformationList);
		}

	}

	@GetMapping("/report/readtable/{tableName}")
	public TableColumnMetaData getTableColumns(@PathVariable String tableName) {

		return reportJDBCService.getTableColumns(tableName);

	}

}