/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.DataType;
import com.opengamma.web.analytics.formatting.ResultsFormatter;

/**
 *
 */
public class GridColumnsJsonWriter {

  /** For looking up the {@link DataType} for a column. */
  private final ResultsFormatter _formatter;

  /**
   * @param formatter For looking up the {@link DataType} for a column
   */
  public GridColumnsJsonWriter(ResultsFormatter formatter) {
    ArgumentChecker.notNull(formatter, "converters");
    _formatter = formatter;
  }

  /**
   * [{name: groupName, columns: [header: colHeader, description: colDescription]}, ...]
   * @param groups Column groups to render to JSON.
   * @return groups as JSON
   */
  public String getJson(List<GridColumnGroup> groups) {
    List<Map<String, Object>> groupList = Lists.newArrayList();
    for (GridColumnGroup group : groups) {
      Map<String, Object> groupMap = Maps.newHashMap();
      groupMap.put("name", group.getName());
      groupMap.put("dependencyGraphsAvailable", group.isDependencyGraphsAvailable());
      List<Map<String, String>> columnList = Lists.newArrayList();
      for (GridColumn column : group.getColumns()) {
        Map<String, String> columnMap = Maps.newHashMap();
        columnMap.put("header", column.getHeader());
        columnMap.put("description", column.getDescription());
        Class<?> columnType = column.getType();
        String type = _formatter.getDataType(columnType).name();
        columnMap.put("type", type);
        columnList.add(columnMap);
      }
      groupMap.put("columns", columnList);
      groupList.add(groupMap);
    }
    return new JSONArray(groupList).toString();
  }
}
