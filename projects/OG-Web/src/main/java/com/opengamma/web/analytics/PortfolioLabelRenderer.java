/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Renders cells in the portfolio grid label column.
 */
/* package */ class PortfolioLabelRenderer implements GridColumn.CellRenderer {

  /** The rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  // TODO it would be better to pass in an interface so the renderers don't have to be rebuilt when the rows change
  /* package */ PortfolioLabelRenderer(List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex,
                                TypeFormatter.Format format,
                                ResultsCache cache,
                                Class<?> columnType,
                                Object inlineKey) {
    PortfolioGridRow row = _rows.get(rowIndex);
    ComputationTargetReference target = row.getTarget();
    ComputationTargetType targetType = target.getType();
    // TODO do I need to use the target type to figure out the row type? can I just have different row types?
    if (targetType.isTargetType(ComputationTargetType.POSITION)) {
      RowTarget rowTarget;
      UniqueId securityId = row.getSecurityId();
      ResultsCache.Result securityResult = cache.getEntity(securityId.getObjectId());
      Security security = (Security) securityResult.getValue();
      if (isOtc(security)) {
        // TODO different type for OTC positions with no trades? they are effecively the same but the client
        // needs to know when a position has no trades because it will be a different endpoint to trigger editing
        // OTC trades and positions are shown as a single row as there's always one trade per position
        rowTarget = new OtcTradeTarget(row.getName(), row.getNodeId(), row.getPositionId(), row.getTradeId());
      } else {
        // Positions in fungible trades can contain multiple trades so the position has its own row and child rows
        // for each of its trades
        rowTarget = new PositionTarget(row.getName(), row.getNodeId(), row.getPositionId());
      }
      // TODO check the cache items for the position, security, underlying to find out whether they've been updated
      return ResultsCell.forStaticValue(rowTarget, columnType, format);
    } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
      return ResultsCell.forStaticValue(new NodeTarget(row.getName(), row.getNodeId()), columnType, format);
    } else if (targetType.isTargetType(ComputationTargetType.TRADE)) {
      // only fungible trades have their own row, OTC trades are shown on the same row as their parent position
      FungibleTradeTarget tradeTarget =
          new FungibleTradeTarget(row.getName(), row.getNodeId(), row.getPositionId(), row.getTradeId());
      // TODO check cache item for trade to see if it's been updated
      return ResultsCell.forStaticValue(tradeTarget, columnType, format);
    }
    throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
  }

  private static boolean isOtc(Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }
}
