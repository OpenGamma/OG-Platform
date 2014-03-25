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
 * Bond clean price from the conventional yield-to-maturity function.
 */
public final class CleanPriceFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {
  /** A singleton instance */
  private static final CleanPriceFromYieldCalculator INSTANCE = new CleanPriceFromYieldCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static CleanPriceFromYieldCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private CleanPriceFromYieldCalculator() {
  }

  /** Calculator from bonds */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();
  /** Calculator from inflation bonds */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return METHOD_BOND_SECURITY.cleanPriceFromYield(bond, yield) * 100;
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return METHOD_BOND_SECURITY.cleanPriceFromYield(bond.getBondStandard(), yield) * 100;
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    ArgumentChecker.notNull(bond.getBondStandard() instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");
    final BondCapitalIndexedSecurity<?> bondSecurity = bond.getBondStandard();
    return METHOD_INFLATION_BOND_SECURITY.cleanPriceFromYield(bondSecurity, yield) * 100.0;
  }

}
