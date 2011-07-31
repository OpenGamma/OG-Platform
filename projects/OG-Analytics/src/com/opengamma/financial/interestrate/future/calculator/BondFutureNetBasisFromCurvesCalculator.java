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
public final class BondFutureNetBasisFromCurvesCalculator extends AbstractInterestRateDerivativeVisitor<Double, double[]> {
  private static final BondFutureNetBasisFromCurvesCalculator INSTANCE = new BondFutureNetBasisFromCurvesCalculator();
  private static final BondFutureSecurityDiscountingMethod CALCULATOR = BondFutureSecurityDiscountingMethod.getInstance();

  public static BondFutureNetBasisFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  private BondFutureNetBasisFromCurvesCalculator() {
  }

  public double[] visitBondFutureSecurity(final BondFutureSecurity bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(bondFuture, "bond future");
    final double futurePrice = CALCULATOR.priceFromCurves(bondFuture, curves);
    return CALCULATOR.netBasisFromCurves(bondFuture, curves, futurePrice);
  }
}
