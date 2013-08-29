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
 *
 */
public final class DirtyPriceFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {
  private static final DirtyPriceFromYieldCalculator INSTANCE = new DirtyPriceFromYieldCalculator();

  public static DirtyPriceFromYieldCalculator getInstance() {
    return INSTANCE;
  }

  private DirtyPriceFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return BondSecurityDiscountingMethod.getInstance().dirtyPriceFromYield(bond, yield);
  }
}
