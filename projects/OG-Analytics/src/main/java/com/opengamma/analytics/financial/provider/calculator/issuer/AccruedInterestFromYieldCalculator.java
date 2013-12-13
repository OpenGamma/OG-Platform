/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate convexity from the yield.
 */
public final class AccruedInterestFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final AccruedInterestFromYieldCalculator s_instance = new AccruedInterestFromYieldCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static AccruedInterestFromYieldCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private AccruedInterestFromYieldCalculator() {
  }

  /** The method used for bonds */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return METHOD_BOND_SECURITY.accruedInterestFromYield(bond, yield);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return METHOD_BOND_SECURITY.accruedInterestFromYield(bond.getBondTransaction(), yield);
  }
}
