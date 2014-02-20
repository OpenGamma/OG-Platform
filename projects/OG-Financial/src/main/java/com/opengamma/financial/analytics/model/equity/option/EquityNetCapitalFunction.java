/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * Prototype - Takes {@link ValueRequirementNames#NET_MARKET_VALUE} as input requirement, 
 * and scales by a constant representing, say, a fund's capital.<p>
 * <p>
 * Applies only to Equity Security Types
 */
public class EquityNetCapitalFunction extends NetCapitalFunction {

  protected String getOutputName() {
    return ValueRequirementNames.EQUITY_NET_CAPITAL;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    Security security = target.getPositionOrTrade().getSecurity();
    if ((security instanceof EquitySecurity) || 
        (security instanceof EquityOptionSecurity) || 
        (security instanceof EquityIndexOptionSecurity)) {
      return true;
    }
    return false;
  }
}
