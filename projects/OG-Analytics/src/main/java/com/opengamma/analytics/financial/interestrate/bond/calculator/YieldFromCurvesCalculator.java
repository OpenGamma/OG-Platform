/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * Calculate bond yield from curves.
 * @deprecated Use {@link com.opengamma.analytics.financial.provider.calculator.issuer.YieldFromCurvesCalculator}
 */
@Deprecated
public final class YieldFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

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

  /** Bill calculator */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  /** Bond calculator */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    return METHOD_BOND_SECURITY.yieldFromCurves(bond, curves);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final YieldCurveBundle curves) {
    return METHOD_BILL_SECURITY.yieldFromCurves(bill, curves);
  }

}
