/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;

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
        if (!FinancialSecurityUtils.isExchangedTraded(positionSecurity) && !(positionSecurity instanceof BondSecurity)) {
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

  /**
   * Declared implementation.
   */
  public static class Impl extends MissingInputsFunction {

    public Impl() {
      super((FunctionDefinition) new PortfolioExchangeTradedDailyPnLFunction());
    }

  }

}
