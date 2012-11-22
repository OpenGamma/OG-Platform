/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static com.opengamma.web.analytics.formatting.DataType.UNKNOWN;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.web.analytics.formatting.DataType;
import com.opengamma.web.analytics.formatting.ResultsFormatter;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.server.conversion.DoubleValueOptionalDecimalPlaceFormatter;

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
  private static final String POSITION_ID = "positionId";
  private static final String NODE_ID = "nodeId";
  private static final String CALCULATION_DURATION = "calculationDuration";

  private final ResultsFormatter _formatter;
  private final DoubleValueOptionalDecimalPlaceFormatter _durationFormatter = new DoubleValueOptionalDecimalPlaceFormatter();

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
      formattedValue = _formatter.format(cellValue, cellValueSpec, viewportResults.getFormat());
      Collection<Object> history = cell.getHistory();
      Class<?> columnType = viewportResults.getColumnType(cell.getColumn());
      DataType columnFormat = _formatter.getDataType(columnType);
      Map<String, Object> valueMap = Maps.newHashMap();
      valueMap.put(VALUE, formattedValue);
      if (columnFormat == UNKNOWN) {
        // if the the column type isn't known then send the type with the value
        valueMap.put(TYPE, _formatter.getDataTypeForValue(cellValue, cellValueSpec).name());
      }
      if (history != null) {
        valueMap.put(HISTORY, formatHistory(cellValueSpec, history));
      }
      if (cell.isError() || isError(formattedValue)) {
        valueMap.put(ERROR, true);
      }
      if (cell.getPositionId() != null) {
        valueMap.put(POSITION_ID, cell.getPositionId());
      }
      if (cell.getNodeId() != null) {
        valueMap.put(NODE_ID, cell.getNodeId());
      }
      // TODO add logging metadata to results
      // TODO add full logging output to results
      results.add(valueMap);
    }
    String duration = _durationFormatter.format(new BigDecimal(viewportResults.getCalculationDuration().toMillisLong()));
    ImmutableMap<String, Object> resultsMap = ImmutableMap.of(VERSION, viewportResults.getVersion(),
                                                              CALCULATION_DURATION, duration,
                                                              DATA, results);
    return new JSONObject(resultsMap).toString();
  }

  private static boolean isError(Object value) {
    return value instanceof MissingInput;
  }

  /**
   * Formats history data as a JSON list.
   * @param cellValueSpec The cell's value specification, can be null
   * @param history The history values, not null
   * @return The formatted history
   */
  private List<Object> formatHistory(ValueSpecification cellValueSpec, Collection<Object> history) {
    List<Object> formattedHistory = Lists.newArrayListWithCapacity(history.size());
    for (Object historyValue : history) {
      formattedHistory.add(_formatter.format(historyValue, cellValueSpec, TypeFormatter.Format.HISTORY));
    }
    return formattedHistory;
  }
}
