package com.docusign.report.db.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.docusign.report.common.exception.CreateTableException;
import com.docusign.report.db.domain.CacheLogInformation;
import com.docusign.report.domain.TableDefinition;

@Service
public class DynamicTableTransformer {

	public String createTableQuery(TableDefinition tableDefinition) {

		List<String> requiredColumnsList = Stream
				.of("recordid,createddatetime,createdby,accountid,batchid,processid".split(",")).map(String::trim)
				.collect(Collectors.toList());

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("CREATE TABLE ");
		stringBuilder.append(tableDefinition.getTableName());
		stringBuilder.append(" ( ");
		tableDefinition.getColumns().forEach(col -> {

			stringBuilder.append("\"");
			stringBuilder.append(col.getColumnName());
			stringBuilder.append("\"");
			stringBuilder.append(" ");
			stringBuilder.append(col.getColumnType());
			stringBuilder.append(", ");

			requiredColumnsList.remove(col.getColumnName().toLowerCase());
		});

		if (!requiredColumnsList.isEmpty()) {

			throw new CreateTableException(
					requiredColumnsList + " is not empty, some required columns are not set in the createtable json");
		}

		if (null != tableDefinition.getPrimaryKey() && !tableDefinition.getPrimaryKey().isEmpty()) {

			stringBuilder.append("PRIMARY KEY");
			stringBuilder.append("(");

			tableDefinition.getPrimaryKey().forEach(key -> {

				stringBuilder.append("\"");
				stringBuilder.append(key);
				stringBuilder.append("\"");
				stringBuilder.append(",");
			});

			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append(")");
		}

		stringBuilder.append(" );");
		return stringBuilder.toString();
	}

	public List<CacheLogInformation> createCacheLogInfoList(TableDefinition tableDefinition) {

		List<CacheLogInformation> cacheLogInformationList = new ArrayList<CacheLogInformation>();
		tableDefinition.getColumns().forEach(col -> {

			if (!StringUtils.isEmpty(col.getColumnName()) && !StringUtils.isEmpty(col.getCsvHeaderName())) {

				CacheLogInformation cacheLogInformation = new CacheLogInformation();
				cacheLogInformation.setCacheKey(col.getColumnName());
				cacheLogInformation.setCacheValue(col.getCsvHeaderName());
				cacheLogInformation.setCacheReference("ColumnName");

				cacheLogInformationList.add(cacheLogInformation);
			}
		});

		return cacheLogInformationList;
	}
}