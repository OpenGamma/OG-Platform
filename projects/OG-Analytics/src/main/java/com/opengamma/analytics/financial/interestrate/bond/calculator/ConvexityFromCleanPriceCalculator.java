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
 * Calculate convexity for bonds from the clean price.
 */
public final class ConvexityFromCleanPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final ConvexityFromCleanPriceCalculator s_instance = new ConvexityFromCleanPriceCalculator();
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();
  /**
   * The fixed coupon bond method.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ConvexityFromCleanPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ConvexityFromCleanPriceCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    return METHOD_BOND.convexityFromCleanPrice(bond, curves) / 100;
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    return METHOD_BOND.convexityFromCleanPrice(bond.getBondTransaction(), curves) / 100;
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    ArgumentChecker.notNull(bond.getBondStandard() instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");

    final BondCapitalIndexedSecurity<?> bondSecurity = bond.getBondStandard();
    return METHOD_INFLATION_BOND_SECURITY.convexityFromCleanPrice(bondSecurity, yield) / 100;
  }

  @Override
  public Double visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    ArgumentChecker.notNull(bond instanceof BondCapitalIndexedSecurity<?>, "the bond should be a BondCapitalIndexedSecurity");
    return METHOD_INFLATION_BOND_SECURITY.convexityFromCleanPrice(bond, yield) / 100;
  }
}
