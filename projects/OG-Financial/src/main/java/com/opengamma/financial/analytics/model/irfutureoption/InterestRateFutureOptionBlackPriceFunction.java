/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.BlackPriceCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Displays the Black price of the Security underlying the trade or position
 * 
 */
public class InterestRateFutureOptionBlackPriceFunction extends InterestRateFutureOptionBlackFunction {

  private static final BlackPriceCalculator CALCULATOR = BlackPriceCalculator.getInstance();

  public InterestRateFutureOptionBlackPriceFunction() {
    super(ValueRequirementNames.SECURITY_MODEL_PRICE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final Double price = irFutureOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, price));
  }

}
