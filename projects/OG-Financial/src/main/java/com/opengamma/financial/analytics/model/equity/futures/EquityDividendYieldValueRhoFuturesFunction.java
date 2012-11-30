/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import com.opengamma.analytics.financial.equity.future.pricing.DividendYieldFuturesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class EquityDividendYieldValueRhoFuturesFunction extends EquityDividendYieldFuturesFunction<Double> {

  public EquityDividendYieldValueRhoFuturesFunction() {
    super(ValueRequirementNames.VALUE_RHO, DividendYieldFuturesCalculator.RatesDeltaCalculator.getInstance());
  }

}
