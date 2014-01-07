/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcleanprice;

import static com.opengamma.engine.value.ValueRequirementNames.YTM;

import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the yield of a bond from the clean price.
 */
public class BondYieldFromCleanPriceFunction extends BondFromCleanPriceFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#YTM}
   * and the calculator to {@link YieldFromCleanPriceCalculator}
   */
  public BondYieldFromCleanPriceFunction() {
    super(YTM, YieldFromCleanPriceCalculator.getInstance());
  }

}
