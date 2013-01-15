/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class QuantityRenderer implements AnalyticsColumn.CellRenderer {

  private final int _colIndex;
  private final List<MainGridStructure.Row> _rows;

  /* package */ QuantityRenderer(int colIndex, List<MainGridStructure.Row> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _colIndex = colIndex;
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache) {
    MainGridStructure.Row row = _rows.get(rowIndex);
    return ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), null, _colIndex, false);
  }
}
