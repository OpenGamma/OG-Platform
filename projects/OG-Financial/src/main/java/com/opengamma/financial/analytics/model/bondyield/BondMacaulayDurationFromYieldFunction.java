/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;

import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the Macaulay duration of a bond from the yield.
 */
public class BondMacaulayDurationFromYieldFunction extends BondFromYieldFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#MACAULAY_DURATION}
   * and the calculator to {@link MacaulayDurationFromYieldCalculator}
   */
  public BondMacaulayDurationFromYieldFunction() {
    super(MACAULAY_DURATION, MacaulayDurationFromYieldCalculator.getInstance());
  }


}
