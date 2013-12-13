/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;

import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the Macaulay duration of a bond from the clean price.
 */
public class BondMacaulayDurationFromCleanPriceFunction extends BondFromCleanPriceFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#MACAULAY_DURATION}
   * and the calculator to {@link MacaulayDurationFromCleanPriceCalculator}
   */
  public BondMacaulayDurationFromCleanPriceFunction() {
    super(MACAULAY_DURATION, MacaulayDurationFromCleanPriceCalculator.getInstance());
  }


}
