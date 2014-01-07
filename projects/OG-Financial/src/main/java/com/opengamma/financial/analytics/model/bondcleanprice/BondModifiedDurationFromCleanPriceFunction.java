/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;

import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the modified duration of a bond from the clean price.
 */
public class BondModifiedDurationFromCleanPriceFunction extends BondFromCleanPriceFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#MODIFIED_DURATION}
   * and the calculator to {@link ModifiedDurationFromCleanPriceCalculator}
   */
  public BondModifiedDurationFromCleanPriceFunction() {
    super(MODIFIED_DURATION, ModifiedDurationFromCleanPriceCalculator.getInstance());
  }

}
