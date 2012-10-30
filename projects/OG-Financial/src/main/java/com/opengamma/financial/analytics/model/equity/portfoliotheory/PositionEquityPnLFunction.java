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
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;

/**
 * 
 */
public class PositionEquityPnLFunction extends AbstractPositionPnLFunction {

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    // REVIEW 2012-10-10 Andrew -- why these checks here; an instance of EquitySecurity won't be any of these?
    if (security instanceof FXForwardSecurity || security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity) {
      return false;
    }
    return security instanceof EquitySecurity;
  }
  
  @Override
  public String getShortName() {
    return "PositionDailyEquityPnL";
  }

}
