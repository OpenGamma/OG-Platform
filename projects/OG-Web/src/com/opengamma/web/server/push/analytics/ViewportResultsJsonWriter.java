/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.server.push.analytics.formatting.Formatter;
import com.opengamma.web.server.push.analytics.formatting.ResultsFormatter;

/**
 * Creates a JSON object from an instance of {@link ViewportResults}.
 */
public class ViewportResultsJsonWriter {

  public static final String VERSION = "version";

  private static final String VALUE = "v";
  private static final String HISTORY = "h";
  private static final String TYPE = "t";
  private static final String DATA = "data";
  private static final String ERROR = "error";

  private final ResultsFormatter _formatter;

  public ViewportResultsJsonWriter(ResultsFormatter formatter) {
    _formatter = formatter;
  }

  public String getJson(ViewportResults viewportResults) {
    List<ViewportResults.Cell> viewportCells = viewportResults.getResults();
    List<Object> results = Lists.newArrayListWithCapacity(viewportCells.size());
    for (ViewportResults.Cell cell : viewportCells) {
      Object formattedValue;
      Object cellValue = cell.getValue();
      ValueSpecification cellValueSpec = cell.getValueSpecification();
      if (viewportResults.isExpanded()) {
        formattedValue = _formatter.formatForExpandedDisplay(cellValue, cellValueSpec);
      } else {
        formattedValue = _formatter.formatForDisplay(cellValue, cellValueSpec);
      }
      Collection<Object> history = cell.getHistory();
      Class<?> columnType = viewportResults.getColumnType(cell.getColumn());
      if (columnType == null || history != null || cell.isError()) {
        // if there is history, an error or we need to send type info then we need to send an object, not just the value
        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.put(VALUE, formattedValue);
        // if the the column type isn't known then send the type with the value
        if (columnType == null) {
          Formatter.FormatType formatType;
          if (cellValue == null) {
            formatType = Formatter.FormatType.PRIMITIVE;
          } else {
            formatType = _formatter.getFormatType(cellValue.getClass());
          }
          valueMap.put(TYPE, formatType.name());
        }
        if (history != null) {
          List<Object> formattedHistory = Lists.newArrayListWithCapacity(history.size());
          for (Object historyValue : history) {
            formattedHistory.add(_formatter.formatForHistory(historyValue, cellValueSpec));
          }
          valueMap.put(HISTORY, formattedHistory);
        }
        if (cell.isError()) {
          valueMap.put(ERROR, true);
        }
        results.add(valueMap);
      } else {
        results.add(formattedValue);
      }
    }
    ImmutableMap<String, Object> resultsMap = ImmutableMap.of(VERSION, viewportResults.getVersion(), DATA, results);
    return new JSONObject(resultsMap).toString();
  }
}
