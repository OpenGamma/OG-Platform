/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate modified duration from the yield.
 */
public final class ModifiedDurationFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final ModifiedDurationFromYieldCalculator s_instance = new ModifiedDurationFromYieldCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ModifiedDurationFromYieldCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ModifiedDurationFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.modifiedDurationFromYield(bond, yield);
  }
}
