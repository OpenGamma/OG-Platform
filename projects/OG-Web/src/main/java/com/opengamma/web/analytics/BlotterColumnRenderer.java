/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.BlotterColumn;
import com.opengamma.web.analytics.blotter.BlotterColumnMapper;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 *
 */
/* package */ class BlotterColumnRenderer implements GridColumn.CellRenderer {

  /** Maps the shared blotter columns to the fields of each different security type. */
  private final BlotterColumnMapper _columnMappings;
  /** The column whose values are handled by this renderer. */
  private final BlotterColumn _column;
  /** The rows in the grid. */
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
  public ResultsCell getResults(int rowIndex,
                                TypeFormatter.Format format,
                                ResultsCache cache,
                                Class<?> columnType,
                                Object inlineKey) {
    PortfolioGridRow row = _rows.get(rowIndex);
    UniqueId securityId = row.getSecurityId();
    Security security;
    ResultsCache.Result result;
    boolean updated;
    if (securityId != null) {
      result = cache.getEntity(securityId.getObjectId());
      security = (Security) result.getValue();
      updated = result.isUpdated();
    } else {
      security = null;
      updated = false;
    }
    return ResultsCell.forStaticValue(_columnMappings.valueFor(_column, security), columnType, format, updated);
  }
}
