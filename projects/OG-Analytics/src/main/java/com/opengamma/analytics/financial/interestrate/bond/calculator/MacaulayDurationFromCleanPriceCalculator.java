/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;

/**
 * Calculate Macaulay duration from clean price.
 */
public final class MacaulayDurationFromCleanPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final MacaulayDurationFromCleanPriceCalculator s_instance = new MacaulayDurationFromCleanPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static MacaulayDurationFromCleanPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private MacaulayDurationFromCleanPriceCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double cleanPrice) {
    return METHOD_BOND_SECURITY.macaulayDurationFromCleanPrice(bond, cleanPrice);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double cleanPrice) {
    return METHOD_BOND_SECURITY.macaulayDurationFromCleanPrice(bond.getBondTransaction(), cleanPrice);
  }
}
