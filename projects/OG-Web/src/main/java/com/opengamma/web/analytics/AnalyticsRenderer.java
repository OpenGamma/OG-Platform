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
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * A renderer for analytics.
 */
/* package */ class AnalyticsRenderer implements GridColumn.CellRenderer {

  private final ColumnSpecification _columnKey;
  private final TargetLookup _targetLookup;

  /* package */ AnalyticsRenderer(ColumnSpecification columnKey, TargetLookup targetLookup) {
    ArgumentChecker.notNull(columnKey, "columnKey");
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    _targetLookup = targetLookup;
    _columnKey = columnKey;
  }

  @Override
  public ResultsCell getResults(int rowIndex,
                                TypeFormatter.Format format,
                                ResultsCache cache,
                                Class<?> columnType,
                                Object inlineKey) {
    Pair<String, ValueSpecification> cellTarget = _targetLookup.getTargetForCell(rowIndex, _columnKey);
    if (cellTarget != null) {
      String calcConfigName = cellTarget.getFirst();
      ValueSpecification valueSpec = cellTarget.getSecond();
      ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
      Object value = cacheResult.getValue();
      return ResultsCell.forCalculatedValue(value,
                                            valueSpec,
                                            cacheResult.getHistory(),
                                            cacheResult.getAggregatedExecutionLog(),
                                            cacheResult.isUpdated(),
                                            columnType,
                                            inlineKey, format);
    } else {
      Collection<Object> emptyHistory = cache.emptyHistory(columnType);
      return ResultsCell.empty(emptyHistory, columnType);
    }
  }
}
