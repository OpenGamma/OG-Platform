/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.pnl.AbstractPortfolioPnLFunction;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 *
 */
public class PortfolioEquityPnLFunction extends AbstractPortfolioPnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    for (final Position position : allPositions) {
      if (position.getSecurity() instanceof EquitySecurity) {
        for (final Trade trade : position.getTrades()) {
          if (!(trade.getSecurity() instanceof EquitySecurity)) {
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
    return "PortfolioDailyEquityPnL";
  }

}
