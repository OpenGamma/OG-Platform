/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class PositionExchangeTradedPnLFunction extends AbstractPositionPnLFunction {

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    Security security = target.getPositionOrTrade().getSecurity();
     
    boolean value = (target.getType() == ComputationTargetType.POSITION && FinancialSecurityUtils.isExchangedTraded(security));
    return value;
  }
  
  @Override
  public String getShortName() {
    return "PositionDailyEquityPnL";
  }

}
