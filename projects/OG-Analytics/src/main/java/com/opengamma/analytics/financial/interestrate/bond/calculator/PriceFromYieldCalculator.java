/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;

/**
 *
 */
public class PriceFromYieldCalculator {

  /**
   * Compute the bill price from the yield. The price is the relative price at settlement.
   * @param convention The yield convention.
   * @param yield The yield in the bill yield convention.
   * @param accrualFactor The accrual factor between settlement and maturity.
   * @return The price.
   */
  public static double priceFromYield(final YieldConvention convention, final double yield, final double accrualFactor) {
    if (convention == SimpleYieldConvention.DISCOUNT) {
      return 1.0 - accrualFactor * yield;
    }
    if (convention == SimpleYieldConvention.INTERESTATMTY) {
      return 1.0 / (1 + accrualFactor * yield);
    }
    throw new UnsupportedOperationException("The convention " + convention.getName() + " is not supported.");
  }

}
