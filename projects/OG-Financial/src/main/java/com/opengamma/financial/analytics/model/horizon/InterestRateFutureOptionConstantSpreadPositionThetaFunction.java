/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * As {@link InterestRateFutureOptionConstantSpreadThetaFunction}, except produces {@link ValueRequirementNames#POSITION_THETA} as a Double
 */
public class InterestRateFutureOptionConstantSpreadPositionThetaFunction extends InterestRateFutureOptionConstantSpreadThetaFunction {

  public InterestRateFutureOptionConstantSpreadPositionThetaFunction() {
    setValueRequirement(ValueRequirementNames.POSITION_THETA);
  }
  
  @Override
  protected Object getValue(final MultipleCurrencyAmount theta, final Currency currency) {
    return theta.getAmount(currency);
  }
}
