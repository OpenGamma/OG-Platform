/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_INTEREST;

import com.opengamma.analytics.financial.interestrate.bond.calculator.AccruedInterestFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the accrued interest of a bond from the yield.
 */
public class BondAccruedInterestFromYieldFunction extends BondFromYieldFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#ACCRUED_INTEREST}
   * and the calculator to {@link AccruedInterestFromYieldCalculator}
   */
  public BondAccruedInterestFromYieldFunction() {
    super(ACCRUED_INTEREST, AccruedInterestFromYieldCalculator.getInstance());
  }


}
