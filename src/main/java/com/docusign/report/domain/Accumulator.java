package com.docusign.report.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class Accumulator {

	private String columnName = null;
	private Integer rowLimitValue = 100000;

	public Accumulator(String columnName, Integer rowLimitValue) {

		this.columnName = columnName;
		this.rowLimitValue = rowLimitValue;
	}

	public void accept(List<Pair<Integer, List<Map<String, Object>>>> lPair, Map<String, Object> tr) {

		Pair<Integer, List<Map<String, Object>>> lastPair = lPair.isEmpty() ? null : lPair.get(lPair.size() - 1);
		Integer amount = 1;
		if (!StringUtils.isEmpty(this.columnName)) {

			amount = (Integer) tr.get(this.columnName);
		}
		if (Objects.isNull(lastPair) || lastPair.getLeft() + amount > this.rowLimitValue) {
			lPair.add(new Pair<Integer, List<Map<String, Object>>>(amount, Arrays.asList(tr)));
		} else {
			List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
			newList.addAll(lastPair.getRight());
			newList.add(tr);
			lastPair.setLeft(lastPair.getLeft() + amount);
			lastPair.setRight(newList);
		}
	}
}