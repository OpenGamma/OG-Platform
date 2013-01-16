/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
/* package */ class AnalyticsRenderer implements GridColumn.CellRenderer {

  private final MainGridStructure _gridStructure;
  private final ColumnKey _columnKey;

  /* package */ AnalyticsRenderer(MainGridStructure gridStructure, ColumnKey columnKey) {
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(columnKey, "columnKey");
    _gridStructure = gridStructure;
    _columnKey = columnKey;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, _columnKey);
    if (cellTarget != null) {
      String calcConfigName = cellTarget.getFirst();
      ValueSpecification valueSpec = cellTarget.getSecond();
      ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
      Object value = cacheResult.getValue();
      return ResultsCell.forCalculatedValue(value,
                                            valueSpec,
                                            cacheResult.getHistory(),
                                            cacheResult.getAggregatedExecutionLog(),
                                            cacheResult.isUpdated(), columnType);
    } else {
      Collection<Object> emptyHistory = cache.emptyHistory(columnType);
      return ResultsCell.empty(emptyHistory, columnType);
    }
  }
}
