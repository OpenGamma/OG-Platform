/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;

/**
 *
 */
public class PureBlackVolatilitySurfaceSecurityDefaults extends PureBlackVolatilitySurfaceDefaults {

  public PureBlackVolatilitySurfaceSecurityDefaults(final String... defaultsPerTicker) {
    super(FinancialSecurityTypes.EQUITY_VARIANCE_SWAP_SECURITY, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final String underlyingId = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(security);
    return getAllTickers().contains(underlyingId);
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return security.getSpotUnderlyingId().getValue();
  }

}
