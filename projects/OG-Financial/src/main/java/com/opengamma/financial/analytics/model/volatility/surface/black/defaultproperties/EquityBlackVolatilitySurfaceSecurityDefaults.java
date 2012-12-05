/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class EquityBlackVolatilitySurfaceSecurityDefaults extends EquityBlackVolatilitySurfaceDefaults {
  //TODO hard-coded to only use bloomberg schemes at the moment

  public EquityBlackVolatilitySurfaceSecurityDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.SECURITY, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (security instanceof EquityIndexOptionSecurity) {
      final EquityIndexOptionSecurity equityIndexOption = (EquityIndexOptionSecurity) security;
      final ExternalId underlyingId = equityIndexOption.getUnderlyingId();
      final String targetScheme = underlyingId.getScheme().getName();
      if (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) || targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
        final String ticker = underlyingId.getValue();
        if (getAllTickers().contains(ticker)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    final EquityIndexOptionSecurity security = (EquityIndexOptionSecurity) target.getSecurity();
    return security.getUnderlyingId().getValue();
  }
}
