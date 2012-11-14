/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.pnl.AbstractPositionPnLFunction;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 *
 */
public class PositionEquityPnLFunction extends AbstractPositionPnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return (security instanceof EquitySecurity) && super.canApplyTo(context, target);
  }

  @Override
  public String getShortName() {
    return "PositionDailyEquityPnL";
  }

}
