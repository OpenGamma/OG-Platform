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
public final class BondFutureNetBasisFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, double[]> {
  private static final BondFutureNetBasisFromCurvesCalculator INSTANCE = new BondFutureNetBasisFromCurvesCalculator();
  private static final BondFutureDiscountingMethod CALCULATOR = BondFutureDiscountingMethod.getInstance();

  public static BondFutureNetBasisFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  private BondFutureNetBasisFromCurvesCalculator() {
  }

  @Override
  public double[] visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(curves, "curves");
    final double futurePrice = CALCULATOR.price(bondFuture, curves);
    return CALCULATOR.netBasisAllBonds(bondFuture, curves, futurePrice);
  }
}
