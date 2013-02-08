/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.BlotterColumn;
import com.opengamma.web.analytics.blotter.BlotterColumnMapper;

/**
 *
 */
/* package */ class BlotterColumnRenderer implements GridColumn.CellRenderer {

  /** Maps  */
  private final BlotterColumnMapper _columnMappings;
  private final BlotterColumn _column;
  private final List<PortfolioGridRow> _rows;

  public BlotterColumnRenderer(BlotterColumn column,
                               BlotterColumnMapper columnMappings,
                               List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(columnMappings, "blotterColumnMappings");
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
    _columnMappings = columnMappings;
    _column = column;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    PortfolioGridRow row = _rows.get(rowIndex);
    Security security = row.getSecurity();
    return ResultsCell.forStaticValue(_columnMappings.valueFor(_column, security), columnType);
  }
}
