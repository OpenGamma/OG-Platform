/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class QuantityRenderer implements GridColumn.CellRenderer {

  private final List<MainGridStructure.Row> _rows;

  /* package */ QuantityRenderer(List<MainGridStructure.Row> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> type) {
    MainGridStructure.Row row = _rows.get(rowIndex);
    return ResultsCell.forStaticValue(row.getQuantity(), type);
  }
}
