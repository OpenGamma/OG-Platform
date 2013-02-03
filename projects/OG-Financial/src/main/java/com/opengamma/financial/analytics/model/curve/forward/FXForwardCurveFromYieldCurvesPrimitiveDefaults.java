/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collection;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardCurveFromYieldCurvesPrimitiveDefaults extends FXForwardCurveFromYieldCurvesDefaults {

  public FXForwardCurveFromYieldCurvesPrimitiveDefaults(final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.UNORDERED_CURRENCY_PAIR, currencyCurveConfigAndDiscountingCurveNames);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UnorderedCurrencyPair ccy = (UnorderedCurrencyPair) target.getValue();
    final Collection<String> currencies = getTargets();
    return currencies.contains(ccy.getFirstCurrency().getCode()) && currencies.contains(ccy.getSecondCurrency().getCode());
  }

  @Override
  protected String getFirstCurrency(final ComputationTarget target) {
    final UniqueId uniqueId = target.getUniqueId();
    final String currencyPair = uniqueId.getValue();
    return currencyPair.substring(0, 3);
  }

  @Override
  protected String getSecondCurrency(final ComputationTarget target) {
    final UniqueId uniqueId = target.getUniqueId();
    final String currencyPair = uniqueId.getValue();
    return currencyPair.substring(3, 6);
  }
}
