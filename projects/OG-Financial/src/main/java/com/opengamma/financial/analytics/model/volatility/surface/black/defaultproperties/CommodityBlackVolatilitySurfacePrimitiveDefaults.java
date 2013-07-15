/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CommodityBlackVolatilitySurfacePrimitiveDefaults extends CommodityBlackVolatilitySurfaceDefaults {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
  };

  public CommodityBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerCurrency) {
    super(ComputationTargetType.CURRENCY, VALUE_REQUIREMENTS, defaultsPerCurrency);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency currency = (Currency) target.getValue();
    return getAllCurrencies().contains(currency.getCode());
  }

  @Override
  protected String getCurrency(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
