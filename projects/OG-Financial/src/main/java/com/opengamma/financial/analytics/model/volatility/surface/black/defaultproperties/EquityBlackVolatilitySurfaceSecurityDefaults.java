/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 *
 */
public class EquityBlackVolatilitySurfaceSecurityDefaults extends EquityBlackVolatilitySurfaceDefaults {

  /**
   * @param defaultsPerTicker The default values for each ticker
   */
  public EquityBlackVolatilitySurfaceSecurityDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.SECURITY, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof EquityIndexOptionSecurity) && !(security instanceof EquityOptionSecurity)) {
      return false;
    }
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(security);
    if (getAllTickers().contains(ticker)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    return EquitySecurityUtils.getIndexOrEquityName(target.getSecurity());
  }
}
