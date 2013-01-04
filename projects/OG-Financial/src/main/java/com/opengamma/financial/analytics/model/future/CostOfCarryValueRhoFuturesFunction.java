/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import com.opengamma.analytics.financial.future.CostOfCarryFuturesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class CostOfCarryValueRhoFuturesFunction extends CostOfCarryFuturesFunction<Double> {

  public CostOfCarryValueRhoFuturesFunction() {
    super(ValueRequirementNames.VALUE_RHO, CostOfCarryFuturesCalculator.RatesDeltaCalculator.getInstance());
  }

}
