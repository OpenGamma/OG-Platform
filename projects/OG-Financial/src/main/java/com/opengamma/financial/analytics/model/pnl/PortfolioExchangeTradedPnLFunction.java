/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 *
 */
public class PortfolioExchangeTradedPnLFunction extends AbstractPortfolioPnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    for (final Position position : allPositions) {
      final Security positionSecurity = position.getSecurity();
      if (FinancialSecurityUtils.isExchangeTraded(positionSecurity)) {
        for (final Trade trade : position.getTrades()) {
          final Security tradeSecurity = trade.getSecurity();
          if (!FinancialSecurityUtils.isExchangeTraded(tradeSecurity)) {
            return false;
          }
        }
      } else {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getShortName() {
    return "PortfolioEquityPnL";
  }

}
