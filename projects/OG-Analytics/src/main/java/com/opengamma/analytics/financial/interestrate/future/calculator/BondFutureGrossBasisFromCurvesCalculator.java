/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class BondFutureGrossBasisFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, double[]> {
  private static final BondFutureGrossBasisFromCurvesCalculator INSTANCE = new BondFutureGrossBasisFromCurvesCalculator();
  private static final BondFutureDiscountingMethod CALCULATOR = BondFutureDiscountingMethod.getInstance();

  public static BondFutureGrossBasisFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  private BondFutureGrossBasisFromCurvesCalculator() {
  }

  @Override
  public double[] visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(bondFuture, "bond future");
    final double futurePrice = CALCULATOR.price(bondFuture, curves);
    return CALCULATOR.grossBasisFromCurves(bondFuture, curves, futurePrice);
  }
}
