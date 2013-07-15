/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.DataType;
import com.opengamma.web.analytics.formatting.ResultsFormatter;

/**
 *
 */
public class GridColumnsJsonWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(GridColumnsJsonWriter.class);

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
    String json = new JSONArray(getJsonStructure(groups)).toString();
    s_logger.debug("Returning JSON for columns {}", json);
    return json;
  }

  /**
   * Returns the underlying data structure used to create the JSON in {@link #getJson}. This allows the JSON to be
   * embedded in another JSON object without
   * @param groups Column groups to render to JSON.
   * @return The groups as a data structure that can easily be converted to JSON
   */
  public List<Map<String, Object>> getJsonStructure(List<GridColumnGroup> groups) {
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
        Integer inlineIndex = column.getInlineIndex();
        if (inlineIndex != null) {
          columnMap.put("inlineIndex", inlineIndex.toString());
        }
        Class<?> columnType = column.getType();
        String type = _formatter.getDataType(columnType).name();
        columnMap.put("type", type);
        columnList.add(columnMap);
      }
      groupMap.put("columns", columnList);
      groupList.add(groupMap);
    }
    return groupList;
  }
}
