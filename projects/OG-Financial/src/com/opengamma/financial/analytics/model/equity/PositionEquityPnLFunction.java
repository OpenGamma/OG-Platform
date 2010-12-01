/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.pnl.AbstractPositionPnLFunction;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * 
 */
public class PositionEquityPnLFunction extends AbstractPositionPnLFunction {

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }
  
  @Override
  public String getShortName() {
    return "PositionDailyEquityPnL";
  }

}
