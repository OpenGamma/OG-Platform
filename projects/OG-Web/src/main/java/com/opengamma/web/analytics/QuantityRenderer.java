/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Cell renderer for the quantity column in the portfolio grid.
 */
/* package */ class QuantityRenderer implements GridColumn.CellRenderer {

  /** The rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ QuantityRenderer(List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    PortfolioGridRow row = _rows.get(rowIndex);
    return ResultsCell.forStaticValue(row.getQuantity(), columnType);
  }
}
