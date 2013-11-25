/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate convexity for bonds.
 * @deprecated Use {@link com.opengamma.analytics.financial.provider.calculator.issuer.ConvexityFromYieldCalculator}
 */
@Deprecated
public final class ConvexityFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final ConvexityFromYieldCalculator s_instance = new ConvexityFromYieldCalculator();
  /**
   * The fixed coupon bond method.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ConvexityFromYieldCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ConvexityFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity yield, final Double curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(yield, "yield");
    return METHOD_BOND.convexityFromYield(yield, curves);
  }

}
