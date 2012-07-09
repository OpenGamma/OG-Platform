/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 *
 */
public class AnalyticsColumnsJsonWriter {

  private final ResultConverterCache _converters;

  public AnalyticsColumnsJsonWriter(ResultConverterCache converters) {
    ArgumentChecker.notNull(converters, "converters");
    _converters = converters;
  }

  /**
   * [{name: groupName, columns: [header: colHeader, description: colDescription]}, ...]
   * @param columns Column groups to render to JSON.
   * @return
   */
  public String getJson(List<AnalyticsColumnGroup> groups) {
    List<Map<String, Object>> groupList = Lists.newArrayList();
    for (AnalyticsColumnGroup group : groups) {
      Map<String, Object> groupMap = Maps.newHashMap();
      groupMap.put("name", group.getName());
      List<Map<String, String>> columnList = Lists.newArrayList();
      for (AnalyticsColumn column : group.getColumns()) {
        Map<String, String> columnMap = Maps.newHashMap();
        columnMap.put("header", column.getHeader());
        columnMap.put("description", column.getDescription());
        Class<?> columnType = column.getType();
        if (columnType != null) {
          ResultConverter<?> converter = _converters.getConverterForType(columnType);
          if (converter != null) {
            columnMap.put("type", converter.getFormatterName());
          }
        }
        columnList.add(columnMap);
      }
      groupMap.put("columns", columnList);
      groupList.add(groupMap);
    }
    return new JSONArray(groupList).toString();
  }
}
