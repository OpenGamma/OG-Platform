/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.List;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.master.position.ManageablePosition;
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
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType, Object inlineKey) {
    PortfolioGridRow row = _rows.get(rowIndex);
    // TODO this might have been updated but forStaticValue() always says updated=false
    // TODO need to get the appropriate ID and get the object with quantity from the cache. how do I know whether it's
    // a trade or position row?
    BigDecimal quantity;
    boolean updated;
    if (row.getTradeId() != null) {
      ResultsCache.Result result = cache.getEntity(row.getTradeId().getObjectId());
      quantity = ((Trade) result.getValue()).getQuantity();
      updated = result.isUpdated();
    } else if (row.getPositionId() != null) {
      ResultsCache.Result result = cache.getEntity(row.getPositionId().getObjectId());
      // this could be SimplePosition or ManageablePosition
      // TODO remove this when the object model is fixed ManageablePosition implements Position
      if (result.getValue() instanceof ManageablePosition) {
        quantity = ((ManageablePosition) result.getValue()).getQuantity();
      } else if (result.getValue() instanceof Position) {
        quantity = ((Position) result.getValue()).getQuantity();
      } else {
        throw new IllegalArgumentException("Unexpected type " + result.getValue().getClass().getName() + " for ID " +
                                               row.getPositionId());
      }
      updated = result.isUpdated();
    } else {
      quantity = null;
      updated = false;
    }
    return ResultsCell.forStaticValue(quantity, columnType, updated);
  }
}
