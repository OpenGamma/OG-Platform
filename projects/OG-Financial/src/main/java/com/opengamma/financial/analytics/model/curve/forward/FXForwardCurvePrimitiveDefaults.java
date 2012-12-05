/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardCurvePrimitiveDefaults extends FXForwardCurveDefaults {

  public FXForwardCurvePrimitiveDefaults(final String... defaultsPerCurrencyPair) {
    super(ComputationTargetType.PRIMITIVE, defaultsPerCurrencyPair);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      final String currencyPair = uniqueId.getValue();
      if (getAllCurrencyPairs().contains(currencyPair)) {
        return true;
      }
      final String firstCcy = currencyPair.substring(0, 3);
      final String secondCcy = currencyPair.substring(3, 6);
      final String reversedCcys = secondCcy + firstCcy;
      return getAllCurrencyPairs().contains(reversedCcys);
    }
    return false;
  }

  @Override
  protected String getCurrencyPair(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
