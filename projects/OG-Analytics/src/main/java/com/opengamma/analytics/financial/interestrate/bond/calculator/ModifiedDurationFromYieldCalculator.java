/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
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

  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.modifiedDurationFromYield(bond, yield);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.modifiedDurationFromYield(bond.getBondTransaction(), yield);
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    ArgumentChecker.notNull(bond.getBondStandard() instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");
    final BondCapitalIndexedSecurity<?> bondSecurity = bond.getBondStandard();
    return METHOD_INFLATION_BOND_SECURITY.modifiedDurationFromYieldFiniteDifference(bondSecurity, yield);
  }

  @Override
  public Double visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    ArgumentChecker.notNull(bond instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");
    return METHOD_INFLATION_BOND_SECURITY.modifiedDurationFromYieldFiniteDifference(bond, yield);
  }
}
