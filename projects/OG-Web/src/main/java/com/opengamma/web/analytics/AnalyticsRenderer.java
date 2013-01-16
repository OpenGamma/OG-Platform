/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
/* package */ class AnalyticsRenderer implements AnalyticsColumn.CellRenderer {

  private final MainGridStructure _gridStructure;
  private final int _colIndex;

  /* package */ AnalyticsRenderer(MainGridStructure gridStructure, int colIndex) {
    _gridStructure = gridStructure;
    _colIndex = colIndex;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache) {
    Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, _colIndex);
    Class<?> columnType = _gridStructure.getColumnType(_colIndex);
    if (cellTarget != null) {
      String calcConfigName = cellTarget.getFirst();
      ValueSpecification valueSpec = cellTarget.getSecond();
      ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
      Object value = cacheResult.getValue();
      return ViewportResults.valueCell(value,
                                       valueSpec,
                                       cacheResult.getHistory(),
                                       cacheResult.getAggregatedExecutionLog(),
                                       _colIndex,
                                       cacheResult.isUpdated());
    } else {
      Collection<Object> emptyHistory = cache.emptyHistory(columnType);
      return ViewportResults.emptyCell(emptyHistory, _colIndex);
    }
  }
}
