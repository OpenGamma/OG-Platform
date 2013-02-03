/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 *
 */
public class PositionExchangeTradedPnLFunction extends AbstractPositionPnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPositionOrTrade().getSecurity();
    return FinancialSecurityUtils.isExchangeTraded(security) && super.canApplyTo(context, target);
  }

  @Override
  public String getShortName() {
    return "PositionDailyEquityPnL";
  }

}
