/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;

/**
 * Calculate bond yield from clean price.
 */
public final class YieldFromCleanPriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final YieldFromCleanPriceCalculator s_instance = new YieldFromCleanPriceCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static YieldFromCleanPriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private YieldFromCleanPriceCalculator() {
  }

  /** Calculator for bills */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  /** Calculator from bonds */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double cleanPrice) {
    return METHOD_BOND_SECURITY.yieldFromCleanPrice(bond, cleanPrice);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double cleanPrice) {
    return METHOD_BOND_SECURITY.yieldFromCleanPrice(bond.getBondStandard(), cleanPrice);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final Double cleanPrice) {
    return METHOD_BILL_SECURITY.yieldFromCleanPrice(bill, cleanPrice);
  }

}
