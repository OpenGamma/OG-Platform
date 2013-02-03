/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXBlackVolatilitySurfacePrimitiveDefaults extends FXBlackVolatilitySurfaceDefaults {

  public FXBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerCurrencyPair) {
    super(ComputationTargetType.UNORDERED_CURRENCY_PAIR, defaultsPerCurrencyPair);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UnorderedCurrencyPair ccy = (UnorderedCurrencyPair) target.getValue();
    String currencyPair = ccy.getFirstCurrency().getCode() + ccy.getSecondCurrency().getCode();
    if (getAllCurrencyPairs().contains(currencyPair)) {
      return true;
    }
    currencyPair = ccy.getSecondCurrency().getCode() + ccy.getFirstCurrency().getCode();
    return getAllCurrencyPairs().contains(currencyPair);
  }


  @Override
  protected String getCurrencyPair(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
