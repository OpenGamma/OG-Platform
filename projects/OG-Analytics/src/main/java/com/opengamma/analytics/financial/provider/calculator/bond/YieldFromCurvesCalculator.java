/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;

/**
 * Calculate dirty price for bonds.
 */
public final class YieldFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final YieldFromCurvesCalculator s_instance = new YieldFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static YieldFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private YieldFromCurvesCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface curves) {
    return METHOD_BOND_SECURITY.yieldFromCurves(bond, curves);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final IssuerProviderInterface curves) {
    return METHOD_BILL_SECURITY.yieldFromCurves(bill, curves);
  }

}
