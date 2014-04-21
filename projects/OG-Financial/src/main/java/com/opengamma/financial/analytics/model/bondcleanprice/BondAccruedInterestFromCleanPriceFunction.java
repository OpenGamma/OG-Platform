/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_INTEREST;

import com.opengamma.analytics.financial.interestrate.bond.calculator.AccruedInterestFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the accrued interest of a bond from the clean price.
 */
public class BondAccruedInterestFromCleanPriceFunction extends BondFromCleanPriceFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#ACCRUED_INTEREST}
   * and the calculator to {@link AccruedInterestFromCleanPriceCalculator}
   */
  public BondAccruedInterestFromCleanPriceFunction() {
    super(ACCRUED_INTEREST, AccruedInterestFromCleanPriceCalculator.getInstance());
  }


}
