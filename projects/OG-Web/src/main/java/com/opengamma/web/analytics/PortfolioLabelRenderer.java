/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Renders cells in the portfolio grid label column.
 */
/* package */ class PortfolioLabelRenderer implements GridColumn.CellRenderer {

  /** The rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ PortfolioLabelRenderer(List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    PortfolioGridRow row = _rows.get(rowIndex);
    ComputationTargetSpecification target = row.getTarget();
    ComputationTargetType targetType = target.getType();
    if (targetType.isTargetType(ComputationTargetType.POSITION)) {
      RowTarget rowTarget;
      if (isOtc(row.getSecurity())) {
        // TODO need the trade ID too
        rowTarget = new OtcTradeTarget(row.getName(), UniqueId.of("ABC", "123"), target.getUniqueId());
      } else {
        rowTarget = new PositionTarget(row.getName(), target.getUniqueId());
      }
      return ResultsCell.forStaticValue(rowTarget, columnType);
    } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
      return ResultsCell.forStaticValue(new NodeTarget(row.getName(), target.getUniqueId()), columnType);
    } else if (targetType.isTargetType(ComputationTargetType.TRADE)) {
      return ResultsCell.forStaticValue(new FungibleTradeTarget(row.getName(), target.getUniqueId()), columnType);
    }
    throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
  }

  private static boolean isOtc(Security security) {
    if (security instanceof FinancialSecurity) {
      return !((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }
}
