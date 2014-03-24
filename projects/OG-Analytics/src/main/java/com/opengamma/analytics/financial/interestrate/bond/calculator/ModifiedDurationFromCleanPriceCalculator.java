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
 * Calculate modified duration from price.
 */
public final class ModifiedDurationFromCleanPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final ModifiedDurationFromCleanPriceCalculator s_instance = new ModifiedDurationFromCleanPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ModifiedDurationFromCleanPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ModifiedDurationFromCleanPriceCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();
  /**
   * The method used for different inflation instruments.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double price) {
    return METHOD_BOND_SECURITY.modifiedDurationFromCleanPrice(bond, price);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double price) {
    return METHOD_BOND_SECURITY.modifiedDurationFromCleanPrice(bond.getBondTransaction(), price);
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction bond, final Double price) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(price, "yield");
    ArgumentChecker.notNull(bond.getBondStandard() instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");

    final BondCapitalIndexedSecurity<?> bondSecurity = (BondCapitalIndexedSecurity<?>) bond.getBondStandard();
    return METHOD_INFLATION_BOND_SECURITY.modifiedDurationFromCleanPrice(bondSecurity, price);
  }

  @Override
  public Double visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final Double price) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(price, "yield");
    ArgumentChecker.notNull(bond instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");

    return METHOD_INFLATION_BOND_SECURITY.modifiedDurationFromCleanPrice(bond, price);
  }
}
