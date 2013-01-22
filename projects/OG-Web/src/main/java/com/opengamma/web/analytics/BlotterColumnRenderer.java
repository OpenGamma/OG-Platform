/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.BlotterColumn;
import com.opengamma.web.analytics.blotter.BlotterColumnMapper;

/**
 *
 */
/* package */ class BlotterColumnRenderer implements GridColumn.CellRenderer {

  private final BlotterColumnMapper _columnMappings;
  private final BlotterColumn _column;
  private final List<PortfolioGridStructure.PortfolioGridRow> _rows;

  public BlotterColumnRenderer(BlotterColumn column,
                               BlotterColumnMapper columnMappings,
                               List<PortfolioGridStructure.PortfolioGridRow> rows) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(columnMappings, "blotterColumnMappings");
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
    _columnMappings = columnMappings;
    _column = column;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    // TODO does the cache need to be modified to contain the flattened portfolio?
    PortfolioGridStructure.PortfolioGridRow row = _rows.get(rowIndex);
    ManageableSecurity security = row.getSecurity();
    return ResultsCell.forStaticValue(_columnMappings.valueFor(_column, security), columnType);
  }
}
