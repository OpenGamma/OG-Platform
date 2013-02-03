/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;

/**
 *
 */
public class CommodityBlackVolatilitySurfaceSecurityDefaults extends CommodityBlackVolatilitySurfaceDefaults {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
  };

  public CommodityBlackVolatilitySurfaceSecurityDefaults(final String... defaultsPerCurrency) {
    super(FinancialSecurityTypes.COMMODITY_FUTURE_OPTION_SECURITY, VALUE_REQUIREMENTS, defaultsPerCurrency);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = ((CommodityFutureOptionSecurity) security).getCurrency().getCode();
    return getAllCurrencies().contains(currency);
  }

  @Override
  protected String getCurrency(final ComputationTarget target) {
    return ((CommodityFutureOptionSecurity) target.getSecurity()).getCurrency().getCode();
  }

}
