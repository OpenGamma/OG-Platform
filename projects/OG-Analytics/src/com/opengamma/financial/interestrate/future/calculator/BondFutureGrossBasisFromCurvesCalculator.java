/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.method.BondFutureSecurityDiscountingMethod;

/**
 * 
 */
public final class BondFutureGrossBasisFromCurvesCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, double[]> {
  private static final BondFutureGrossBasisFromCurvesCalculator INSTANCE = new BondFutureGrossBasisFromCurvesCalculator();
  private static final BondFutureSecurityDiscountingMethod CALCULATOR = BondFutureSecurityDiscountingMethod.getInstance();

  public static BondFutureGrossBasisFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  private BondFutureGrossBasisFromCurvesCalculator() {
  }

  @Override
  public double[] visitBondFutureSecurity(final BondFutureSecurity bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(bondFuture, "bond future");
    final double futurePrice = CALCULATOR.price(bondFuture, curves);
    return CALCULATOR.grossBasisFromCurves(bondFuture, curves, futurePrice);
  }
}
