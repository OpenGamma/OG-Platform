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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class PortfolioExchangeTradedDailyPnLFunction extends AbstractPortfolioDailyPnLFunction {
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() == ComputationTargetType.PORTFOLIO_NODE) {
      final PortfolioNode node = target.getPortfolioNode();
      final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
      for (Position position : allPositions) {
        Security positionSecurity = position.getSecurity();
        if (!FinancialSecurityUtils.isExchangedTraded(positionSecurity)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  @Override
  public String getShortName() {
    return "PortfolioDailyEquityPnL";
  }
  
}
