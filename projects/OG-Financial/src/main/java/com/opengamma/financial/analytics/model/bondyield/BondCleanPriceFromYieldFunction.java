/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;

import com.opengamma.analytics.financial.interestrate.bond.calculator.CleanPriceFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the clean price of a bond from the yield.
 */
public class BondCleanPriceFromYieldFunction extends BondFromYieldFunction<Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#CLEAN_PRICE}
   * and the calculator to {@link CleanPriceFromYieldCalculator}
   */
  public BondCleanPriceFromYieldFunction() {
    super(CLEAN_PRICE, CleanPriceFromYieldCalculator.getInstance());
  }

}
