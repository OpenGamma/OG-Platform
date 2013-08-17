/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;

/**
 * Calculate bond yield from price.
 */
public final class YieldFromPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final YieldFromPriceCalculator s_instance = new YieldFromPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static YieldFromPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private YieldFromPriceCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  /**
   * Yield from clean price.
   */
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double cleanPrice) {
    return METHOD_BOND_SECURITY.yieldFromCleanPrice(bond, cleanPrice);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final Double price) {
    return METHOD_BILL_SECURITY.yieldFromPrice(bill, price);
  }

}
