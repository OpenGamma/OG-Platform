/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;

/**
 * Calculate modified duration from price.
 */
public final class ModifiedDurationFromPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final ModifiedDurationFromPriceCalculator s_instance = new ModifiedDurationFromPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ModifiedDurationFromPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ModifiedDurationFromPriceCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double price) {
    return METHOD_BOND_SECURITY.modifiedDurationFromCleanPrice(bond, price);
  }

}
