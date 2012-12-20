/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * Calculate dirty price for bonds.
 */
public final class YieldFromCurvesCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

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

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.yieldFromCurves(bond, curves);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bill);
    return METHOD_BILL_SECURITY.yieldFromCurves(bill, curves);
  }

}
