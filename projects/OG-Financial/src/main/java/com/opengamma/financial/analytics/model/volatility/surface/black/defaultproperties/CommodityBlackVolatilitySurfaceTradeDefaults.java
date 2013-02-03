/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;

/**
 *
 */
public class CommodityBlackVolatilitySurfaceTradeDefaults extends CommodityBlackVolatilitySurfaceDefaults {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE};

  public CommodityBlackVolatilitySurfaceTradeDefaults(final String... defaultsPerCurrency) {
    super(ComputationTargetType.TRADE, VALUE_REQUIREMENTS, defaultsPerCurrency);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (!(security instanceof CommodityFutureOptionSecurity)) {
      return false;
    }
    final String currency = ((CommodityFutureOptionSecurity) security).getCurrency().getCode();
    return getAllCurrencies().contains(currency);
  }

  @Override
  protected String getCurrency(final ComputationTarget target) {
    return ((CommodityFutureOptionSecurity) target.getTrade().getSecurity()).getCurrency().getCode();
  }

}
