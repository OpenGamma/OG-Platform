/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;

import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the modified duration of a bond from the yield.
 */
public class BondModifiedDurationFromYieldFunction extends BondFromYieldFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#MODIFIED_DURATION}
   * and the calculator to {@link ModifiedDurationFromYieldCalculator}
   */
  public BondModifiedDurationFromYieldFunction() {
    super(MODIFIED_DURATION, ModifiedDurationFromYieldCalculator.getInstance());
  }

}
